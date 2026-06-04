package it.polimi.ingsw.am46.server.controller;

import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.server.model.Color;
import it.polimi.ingsw.am46.server.model.Game;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.OfferTile;
import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.state.RoundPhase;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 Receives commands from clients (via RmiServer or SocketServer),
 calls the Game, catches exceptions, and notifies clients via VirtualView.
 Knows nothing about RMI or Sockets — communicates only with VirtualView.
 All public methods are synchronized to prevent
 data races when multiple clients call simultaneously.
 */

public class ServerController {

    private boolean isResilienceEnabled = false; // if we do resilience, it will turn true
    private boolean gamePaused = false;
    private Game game;
    private VirtualView virtualView;
    // Timer per il caso "ultimo giocatore rimasto"
    private java.util.concurrent.ScheduledFuture<?> lastPlayerTimer;

    /*
     * Single-thread daemon scheduler for the reconnection timer.
     * Using a daemon thread ensures the JVM does not hang on shutdown
     * if the timer fires while the server is closing.
     */
    private final java.util.concurrent.ScheduledExecutorService timerScheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "resilience-timer");
                t.setDaemon(true);
                return t;
            });

    public ServerController() {
        this.game = new Game();
        this.game.setPhaseChangeListener(() -> {
            if (virtualView != null && game.getCurrentPhase().isAutomatic()) {
                try {
                    virtualView.broadcastUpdate(buildGameState());
                } catch (Exception e) { /* ignore */ }
            }
        });
    }

    public void setVirtualView(VirtualView virtualView) {
        this.virtualView = virtualView;
    }

    public void setResilienceEnabled(boolean enabled) {
        this.isResilienceEnabled = enabled;
    }





// COMMANDS FROM CLIENTS


    public synchronized void connect(String nickname,String colorName, NetworkMode cur) throws GameAlreadyStartedException, InvalidConnectionException, NicknameOfflineException {
        System.out.println("[SERVER LOG] Ricevuta richiesta di connessione da: " + nickname);

        if (nickname == null || nickname.isBlank()) {
            throw new InvalidConnectionException("Nickname must not be blank");
        }
        if (cur == null) {
            throw new InvalidConnectionException("Client reference must not be null");
        }
        if (virtualView == null) {
            throw new IllegalStateException("VirtualView is not configured");
        }
        // -------------------------------------------------------
        // RESILIENCE: if a player with this nickname already exists
        // in the model and is marked as disconnected, redirect to
        // the reconnect flow instead of creating a new player slot.
        // -------------------------------------------------------
        Player existing = game.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst().orElse(null);

        if (existing != null) {
            if (existing.isDisconnected()) {
                throw new NicknameOfflineException(nickname);
            } else {
                throw new InvalidConnectionException("Nickname già in uso da un giocatore connesso.");
            }
        }

        if (game.isGameStarted()) {
            throw new GameAlreadyStartedException("Game already started");
        }
        Color chosenColor;
        try {
            chosenColor = Color.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidConnectionException("Colore non valido o inesistente: " + colorName);
        }
        if (!game.getAvailableColors().contains(chosenColor)) {
            throw new InvalidConnectionException("Color already taken!");
        }

        game.addPlayer(nickname);
        Player p = getPlayerByNickname(nickname);
        game.assignColor(p, chosenColor);
        boolean playerAddedToModel = true;

        try {
            if (game.getHostNickname() == null) {
                game.setHostNickname(nickname);
            }

            // 2. REGISTRAZIONE NELLA RETE E BROADCAST
            virtualView.registerClient(nickname, cur);
            System.out.println("[SERVER LOG] Giocatore " + nickname + " aggiunto con successo al tabellone.");

            virtualView.broadcastUpdate(buildGameState());
            tryStartGame();

        } catch (Exception e) {
            if (playerAddedToModel) {
                game.removePlayer(nickname);
            }
            try {
                virtualView.unregisterClient(nickname);
            } catch (Exception cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }

            // Rimbalziamo di nuovo l'errore al chiamante
            throw new InvalidConnectionException("Failed to complete connection for " + nickname + ": " + e.getMessage());
        }
    }

    public synchronized void setExpectedPlayers(String nickname, int numPlayers) {
        try {
            if (nickname == null || nickname.isBlank()) {
                throw new InvalidConnectionException("Nickname must not be blank");
            }
            if (virtualView == null) {
                throw new IllegalStateException("VirtualView is not configured");
            }
            if (game.isGameStarted()) {
                throw new GameAlreadyStartedException("Game already started");
            }
            if (game.getHostNickname() == null || !game.getHostNickname().equals(nickname)) {
                throw new IllegalStateException("Only the host can choose the number of players");
            }
            if (numPlayers < 2 || numPlayers > 5) {
                throw new IllegalArgumentException("Player count must be between 2 and 5");
            }
            if (numPlayers < game.getPlayers().size()) {
                throw new IllegalStateException("There are already more connected players than the selected number");
            }

            game.setExpectedPlayers(numPlayers);
            virtualView.broadcastUpdate(buildGameState());
            tryStartGame();

        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorToClient(nickname, e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set expected players for " + nickname, e);
        }
    }



    /*
     * Handles a totem placement request.
     * Validates the action, calls the Game, and broadcasts the updated state.
     * Sends an error only to the requesting client if validation fails.
     */
    public synchronized void moveTotem(String nickname, String offerTileId) {
        try {
            if (!game.isGameStarted()) {
                throw new IllegalStateException("Game has not started yet");
            }
            if (gamePaused) {
                sendErrorToClient(nickname, "Game is paused: waiting for other players to reconnect.");
                return;
            }
            Player player = getPlayerByNickname(nickname);
            OfferTile offerTile = getOfferTileById(offerTileId);
            game.moveTotem(player, offerTile);
            virtualView.broadcastUpdate(buildGameState());
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorToClient(nickname, e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during moveTotem", e);
        }
    }

    /*
     * Handles a request to take a card from the board.
     * Validates the action, updates the Game, and broadcasts the new state.
     */
    public synchronized void addCard(String nickname, String cardId) {
        try {
            if (!game.isGameStarted()) {
                throw new IllegalStateException("Game has not started yet");
            }
            if (gamePaused) {
                sendErrorToClient(nickname, "Game is paused: waiting for other players to reconnect.");
                return;
            }
            Player player = getPlayerByNickname(nickname);
            Card card = getCardById(cardId);
            if (card == null) {
                throw new IllegalArgumentException("Card ID cannot be null for this action");
            }
            game.addCard(player, card);
            virtualView.broadcastUpdate(buildGameState());
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorToClient(nickname, e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during addCard", e);
        }
    }

    /*
     Handles the optional extra card draw.
     If cardId is null, the player intentionally skips the extra draw.
     */
    public synchronized void addExtraCard(String nickname, String cardId) {
        try {
            if (!game.isGameStarted()) {
                throw new IllegalStateException("Game has not started yet");
            }
            if (gamePaused) {
                sendErrorToClient(nickname, "Game is paused: waiting for other players to reconnect.");
                return;
            }
            Player player = getPlayerByNickname(nickname);
            Card card = getCardById(cardId);
            game.addExtraCard(player, card);
            virtualView.broadcastUpdate(buildGameState());
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorToClient(nickname, e.getMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during addExtraCard", e);
        }
    }


    //Shortcut for skipping the extra draw phase.

    public synchronized void skipExtraDraw(String nickname) {
        addExtraCard(nickname, null);
    }

    /*
     * Handles a client disconnection detected by the RMI layer.
     * Removes the client from the VirtualView and notifies remaining players.
     */
    public synchronized void handleDisconnection(String nickname) {
        if (nickname == null || nickname.isBlank()) return;

        Player player;
        try {
            player = getPlayerByNickname(nickname);
        } catch (IllegalArgumentException e) {
            // Giocatore non in partita, disconnessione ignorata
            return;
        }

        // Guard: se già disconnesso, ignora la chiamata duplicata
        // Evita che ping RMI e heartbeat socket scattino entrambi
        if (player.isDisconnected()) {
            System.out.println("[SERVER LOG] Disconnessione duplicata ignorata per: " + nickname);
            return;
        }

        // if the resilience is enabled, suspendPlayer (resilience), else GAME OVER
        if (!isResilienceEnabled) {
            abortGame(nickname);
        } else {
            suspendPlayer(nickname);
        }
    }

    private void abortGame(String disconnectedNickname){
        System.out.println("[SERVER LOG]: Fatal disconnection from: " + disconnectedNickname + ". GAME OVER");
        if (virtualView != null) {
            try{
                virtualView.broadcastAbort("DISCONNECTION_ERROR : Player " + disconnectedNickname + " is disconnected. GAME OVER.");
                virtualView.clearClients();
            }catch(Exception e){
                //ignore
            }
        }
        this.game = new Game(); // we create a new game so the server is now ready to start a new game
    }


    // for the advanced function resilience
    private void suspendPlayer(String nickname) {
        System.out.println("[SERVER LOG]: Sospensione per disconnessione da: " + nickname + ". Il gioco prosegue.");

        try {
            Player player = getPlayerByNickname(nickname);

            // 1. MARCA COME DISCONNESSO PRIMA DI TUTTO (punto di verità)
            player.setDisconnected(true);

            // 2. Rimuovi il canale di rete morto
            if (virtualView != null) {
                virtualView.unregisterClient(nickname);
            }

            // 3. Gestione pre-game: host dropout
            if (!game.isGameStarted()) {
                if (nickname.equals(game.getHostNickname())) {
                    handleHostDisconnection();
                }
                // Broadcast LOBBY update e STOP (non c'è FSM da gestire)
                if (virtualView != null) {
                    virtualView.broadcastUpdate(buildGameState());
                }
                return;
            }

            // 4. Game in corso: gestisci lo skip del turno
            if (game.getActivePlayer() != null
                    && game.getActivePlayer().getNickname().equals(nickname)) {
                // Il giocatore era ATTIVO: skip immediato tramite FSM
                advancePastDisconnectedPlayer();
            } else {
                // Il giocatore era in CODA: rimuovi silenziosamente dalle strutture
                advancePastQueuedDisconnectedPlayer(player);
            }

            // 5. Conta giocatori online DOPO lo skip
            long onlineCount = game.getPlayers().stream()
                    .filter(p -> !p.isDisconnected())
                    .count();

            if (onlineCount == 0) {
                System.out.println("[RESILIENZA] Tutti offline. Gioco in pausa.");
            } else if (onlineCount == 1) {
                gamePaused = true;
                startLastPlayerTimer(nickname);
            } else {virtualView.broadcastError("Player " + nickname + " has disconnected.");}
            // onlineCount > 1: gioco continua normalmente

            // 6. Broadcast FINALE unico (evita doppie notifiche)
            if (virtualView != null) {
                virtualView.broadcastUpdate(buildGameState());
            }

        } catch (Exception e) {
            System.err.println("[ERRORE] suspendPlayer per " + nickname + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Translates the current FSM phase into the correct skip action.
     *
     * Uses game.skipPlayerTurn() (which delegates to RoundPhase.handleSkipTurn)
     * rather than calling phase-specific methods directly, to preserve the
     * State Pattern boundary.
     *
     * For ExtraDrawState the existing game.addExtraCard(player, null) path is
     * reused because null card means voluntary skip — semantically identical to
     * a forced skip from disconnection.
     */
    private void advancePastDisconnectedPlayer() {
        try {
            if (game == null || game.getCurrentPhase() == null) return;

            RoundPhase phase = game.getCurrentPhase();
            Player disconnected = game.getActivePlayer();
            if (disconnected == null) return;

            String phaseName = phase.getClass().getSimpleName();
            System.out.println("[Resilience] Auto-skipping " + disconnected.getNickname()
                    + " in phase " + phaseName);

            switch (phaseName) {
                case "PlaceTotemState", "AddCardState", "ExtraDrawState" ->
                        game.skipPlayerTurn(disconnected);
                case "ResolveEventState", "EndRoundState" ->
                        System.out.println("[Resilience] Fase automatica, nessun skip richiesto.");
                default ->
                        System.err.println("[Resilience] Unknown phase: " + phaseName);
            }
            // NESSUN broadcastUpdate qui — lo fa suspendPlayer() dopo questo return


        } catch (Exception e) {
            System.err.println("advancePastDisconnectedPlayer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void advancePastQueuedDisconnectedPlayer(Player player) {
        try {
            if (game.getCurrentPhase() == null) return;

            String phaseName = game.getCurrentPhase().getClass().getSimpleName();
            System.out.println("[Resilience] Rimozione dalla coda: " + player.getNickname() + " (fase: " + phaseName + ")");

            switch (phaseName) {
                case "PlaceTotemState" -> {
                    // PlaceTotemState sa gestire un player non attivo:
                    // - rimuove da placementOrder se presente
                    // - sposta il totem su TurnTile se necessario
                    // - NON avanza il turno (lascia invariato l'activePlayer)
                    game.skipPlayerTurn(player);
                }
                case "AddCardState" -> {
                    // AddCardState rimuove il player da drawOrder
                    // e sposta il totem, ma NON chiama advanceTurn()
                    game.skipPlayerTurn(player);
                }
                case "ExtraDrawState" -> {
                    // ExtraDrawState rimuove da eligiblePlayers senza avanzare
                    game.skipPlayerTurn(player);
                }
                default -> {
                    // Fasi automatiche: nessun'azione necessaria
                }
            }

            // NOTA: NON facciamo broadcastUpdate() qui!
            // Il broadcast finale è gestito da suspendPlayer() per evitare duplicati.

        } catch (Exception e) {
            System.err.println("Errore in advancePastQueuedDisconnectedPlayer: " + e.getMessage());
        }
    }


    public synchronized void reconnect(String nickname, NetworkMode cur) {
        try {
            Player player = getPlayerByNickname(nickname);
            player.setDisconnected(false);
            System.out.println("[Resilience] " + nickname + " è tornato ONLINE.");
            //aggiorna la registrazione sul layer di rete
            // (rimuove il vecchio stub RMI/Socket e registra quello nuovo)
            virtualView.unregisterClient(nickname);
            virtualView.registerClient(nickname, cur);

            // Notifica gli altri giocatori della riconnessione
            try {
                virtualView.broadcastError("Player " + nickname + " has reconnected.");
            } catch (Exception e) {System.err.println("[SERVER LOG] Errore durante la notifica di riconnessione di " + nickname + ": " + e.getMessage());}

            // Cancella il timer dei 60s se stava girando
            if (lastPlayerTimer != null && !lastPlayerTimer.isDone()) {
                lastPlayerTimer.cancel(false);
                lastPlayerTimer = null;
                System.out.println("[Resilience] Timer 60s cancellato.");
            }
            // Il broadcastUpdate informa tutti i client incluso quello rientrato,
            // che a questo punto è già stato registrato dal layer di rete chiamante
            gamePaused = false;
            if (virtualView != null) {
                virtualView.broadcastUpdate(buildGameState());
            }

        } catch (Exception e) {
            System.err.println("Errore in reconnect per " + nickname + ": " + e.getMessage());
        }
    }

    /**
     * Starts a 60-second countdown.
     * If no other player reconnects within 60 seconds,
     * the last connected player is declared winner.
     */
    private void startLastPlayerTimer(String nickname) {
        Player lastPlayer = game.getPlayers().stream()
                .filter(p -> !p.isDisconnected())
                .findFirst()
                .orElse(null);

        if (lastPlayer == null) return;

        // 1. Notifica immediata al giocatore rimasto
        try {
            if (virtualView != null) {
                virtualView.sendError(lastPlayer.getNickname(), "Player " + nickname + " has disconnected. ⚠️" + "Sei l'unico giocatore connesso. Vittoria automatica tra 60 secondi se nessuno si ricollega.");
            }
        } catch (Exception e) {
            System.err.println("[Resilience] Impossibile notificare l'ultimo giocatore: " + e.getMessage());
        }

        System.out.println("[Resilience] ⏰ Timer 60s avviato. Ultimo giocatore connesso: " + lastPlayer.getNickname());

        // 2. Schedula il timer
        lastPlayerTimer = timerScheduler.schedule(() -> {
            synchronized (ServerController.this) {
                // Ricalcola onlineCount al momento dello scadere (qualcuno potrebbe essersi ricollegato!)
                long stillOnline = game.getPlayers().stream()
                        .filter(p -> !p.isDisconnected())
                        .count();

                if (stillOnline == 1) {
                    System.out.println("[Resilience] ⏰ Timer scaduto. Dichiarazione vincitore per abbandono.");

                    try {
                        //Calcola i punteggi finali PRIMA di costruire lo stato
                        game.countFinalPoints();
                        game.forceGameOver();

                        //Costruisci il GameState DOPO countFinalPoints e forceGameOver
                        // Assicurati che GameState.PlayerState copi isDisconnected() dal Player!
                        GameState finalState = buildGameState();

                        // CRITICO 3: Usa broadcastWinner (non broadcastUpdate) per segnalare la fine
                        if (virtualView != null) {
                            virtualView.broadcastWinner(finalState);
                        }

                    } catch (Exception e) {
                        System.err.println("Errore durante broadcastWinner dal timer: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("[Resilience] Timer scaduto ma qualcuno si è ricollegato (" + stillOnline + " online). Nessun vincitore automatico.");
                }
            }
        }, 60, TimeUnit.SECONDS);
    }

    /*
     * Assigns the host role to the next available player if the current host disconnects.
     * Resets expectedPlayers to allow the new host to choose.
     */
    private void handleHostDisconnection() {
        if (game.getPlayers().isEmpty()) {
            game.setHostNickname(null);
            game.setExpectedPlayers(null);
        } else {
            // Take the first remaining player as the new host
            String newHost = game.getPlayers().getFirst().getNickname();
            game.setHostNickname(newHost);
            // Reset expected players so the new host can decide again
            game.setExpectedPlayers(null);
        }
    }

// PRIVATE UTILITIES

    /*
     Builds a serializable GameState snapshot from the current Game.
     This object is sent to clients through the network.
     */
    public GameState buildGameState() {
        GameState gs = new GameState(game);
        //game in pausa o no
        gs.setGamePaused(gamePaused);
        return gs;
    }

    private void tryStartGame() throws Exception {
        if (game.isGameStarted() || game.getExpectedPlayers() == null) {
            return;
        }

        if (game.getPlayers().size() == game.getExpectedPlayers()) {
            game.setupGame(game.getExpectedPlayers());
            virtualView.broadcastUpdate(buildGameState());
        }
    }

    /*
     Retrieves a Player by nickname.
     Throws IllegalStateException if the player does not exist.
     */
    private Player getPlayerByNickname(String nickname) {
        return game.getPlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + nickname));
    }

    /*
     * Retrieves an OfferTile by its ID.
     * Throws IllegalStateException if the tile does not exist.
     */
    private OfferTile getOfferTileById(String offerTileId) {
        if (offerTileId == null || offerTileId.isBlank()) {
            throw new IllegalArgumentException("OfferTile ID must not be blank");
        }
        char letter = offerTileId.toUpperCase().charAt(0);
        return game.getBoard().getOfferTiles().stream()
                .filter(t -> t.getLetter() == letter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("OfferTile not found: " + offerTileId));
    }

    /*
     Retrieves a Card by its ID from the board.
     Throws IllegalStateException if the card does not exist.
     */
    private Card getCardById(String cardId) {
        if (cardId == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(cardId);
            return game.getBoard().getTopRow().stream()
                    .filter(c -> c.getId() == id)
                    .findFirst()
                    .or(() -> game.getBoard().getBottomRow().stream().filter(c -> c.getId() == id).findFirst())
                    .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid card ID format: " + cardId);
        }
    }

    /*
     Sends an error message to a specific client.
     If sending fails, the client is considered disconnected.
     */
    private void sendErrorToClient(String nickname, String message) {
        try {
            if (virtualView != null) {
                virtualView.sendError(nickname, message);
            }
        } catch (Exception e) {
            System.out.println("[SERVER LOG]: Fallito invio errore a " + nickname + ". Disconnessione in corso...");
            handleDisconnection(nickname);
        }
    }


    public List<Color> getAvailableColors() throws Exception {
        return game.getAvailableColors();
    }
}