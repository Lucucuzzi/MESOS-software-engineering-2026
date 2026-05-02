package it.polimi.ingsw.am46.controller;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;

/*
 Receives commands from clients (via RmiServer or SocketServer),
 calls the Game, catches exceptions, and notifies clients via VirtualView.
 Knows nothing about RMI or Sockets — communicates only with VirtualView.
 All public methods are synchronized to prevent
 data races when multiple clients call simultaneously.
 */

public class ServerController {

    private final boolean isResilienceEnabled = false; // if we do resilience, it will turn true
    private Game game;
    private VirtualView virtualView;

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


// COMMANDS FROM CLIENTS


    public synchronized void connect(String nickname, NetworkMode cur) throws Exception {
        System.out.println("[SERVER LOG] Ricevuta richiesta di connessione da: " + nickname);

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname must not be blank");
        }
        if (cur == null) {
            throw new IllegalArgumentException("Client reference must not be null");
        }
        if (virtualView == null) {
            throw new IllegalStateException("VirtualView is not configured");
        }
        if (game.isGameStarted()) {
            throw new IllegalStateException("Game already started");
        }

        // 1. TENTA L'AGGIUNTA NEL MODEL.
        // Se il nome è preso, questo lancia IllegalArgumentException ed esce subito.
        // Niente "sendErrorToClient", l'eccezione viene rimbalzata indietro a Socket/RMI.
        game.addPlayer(nickname);

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
            // Se qualcosa va storto DOPO averlo aggiunto al model (es. errore di rete in registerClient)
            // Dobbiamo fare "rollback" per non lasciare un giocatore fantasma nel Game.
            if (playerAddedToModel) {
                game.removePlayer(nickname);
            }
            try {
                virtualView.unregisterClient(nickname);
            } catch (Exception cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }

            // Rimbalziamo di nuovo l'errore al chiamante
            throw new IllegalStateException("Failed to complete connection for " + nickname, e);
        }
    }

    public synchronized void setExpectedPlayers(String nickname, int numPlayers) {
        try {
            if (nickname == null || nickname.isBlank()) {
                throw new IllegalArgumentException("Nickname must not be blank");
            }
            if (virtualView == null) {
                throw new IllegalStateException("VirtualView is not configured");
            }
            if (game.isGameStarted()) {
                throw new IllegalStateException("Game already started");
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
                virtualView.broadcastError("DISCONNECTION_ERROR : Player " + disconnectedNickname + " is disconnected. GAME OVER.");
                virtualView.clearClients();
            }catch(Exception e){
                //ignore
            }
        }
        this.game = new Game(); // we create a new game so the server is now ready to start a new game
    }
    // for the advanced function resilience
    private void suspendPlayer(String disconnectedNickname){
        System.out.println("[SERVER LOG]: Disconnection from: " +disconnectedNickname + ". Game CONTINUES.");
        game.removePlayer(disconnectedNickname);
        if (virtualView != null) {
            virtualView.unregisterClient(disconnectedNickname);
        }

        if (!game.isGameStarted()) {
            if (disconnectedNickname.equals(game.getHostNickname())) {
                handleHostDisconnection();
            }
        }
        if (virtualView != null) {
            try{
                virtualView.broadcastUpdate(buildGameState());
            }catch(Exception e){
                //ignore
            }
        }
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
    private GameState buildGameState() {
        return new GameState(game);
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




}
