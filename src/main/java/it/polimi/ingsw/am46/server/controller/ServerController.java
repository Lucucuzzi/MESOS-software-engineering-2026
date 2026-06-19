package it.polimi.ingsw.am46.server.controller;

import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.server.db.dao.GameResultDAO;
import it.polimi.ingsw.am46.server.db.dao.GameResultDAOimpl;
import it.polimi.ingsw.am46.server.model.Color;
import it.polimi.ingsw.am46.server.model.Game;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.OfferTile;
import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.state.RoundPhase;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 Receives commands from clients (via RmiServer or SocketServer),
 calls the Game, catches exceptions, and notifies clients via VirtualView.
 Knows nothing about RMI or Sockets — communicates only with VirtualView.
 All public methods are synchronized to prevent
 data races when multiple clients call simultaneously.
 */

/**
 * The type Server controller.
 */
public class ServerController {

    private boolean isResilienceEnabled = false; // if we do resilience, it will turn true
    private boolean gamePaused = false;
    private Game game;
    private VirtualView virtualView;
    // Timer for the "last player remaining" case
    private java.util.concurrent.ScheduledFuture<?> lastPlayerTimer;
    private final GameResultDAO gameResultDAO = new GameResultDAOimpl();
    private boolean savedToDb = false;

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

    /**
     * Instantiates a new Server controller.
     */
    public ServerController() {
        this.game = new Game();
        this.game.setPhaseChangeListener(() -> {
            System.out.println("[DB] PhaseChangeListener — isFinalPointsCounted: "
                    + game.isFinalPointsCounted() + " savedToDb: " + savedToDb);
            if (game.isFinalPointsCounted() && !savedToDb) {
                saveGameResults();
                try {
                    GameState winnerState = buildGameState();
                    attachLeaderboardToGameState(winnerState);
                    virtualView.broadcastWinner(winnerState);
                } catch (Exception e) {
                    System.err.println("[PhaseChangeListener] Error broadcastWinner: "
                            + e.getMessage());
                }
                return;
            }
            if (virtualView != null && game.getCurrentPhase().isAutomatic()) {
                try {
                    virtualView.broadcastUpdate(buildGameState());
                } catch (Exception e) { /* ignore */ }
            }
        });
    }

    /**
     * Sets virtual view.
     *
     * @param virtualView the virtual view
     */
    public void setVirtualView(VirtualView virtualView) {
        this.virtualView = virtualView;
    }

    /**
     * Sets resilience enabled.
     *
     * @param enabled the enabled
     */
    public void setResilienceEnabled(boolean enabled) {
        this.isResilienceEnabled = enabled;
    }





// COMMANDS FROM CLIENTS


    /**
     * Connect.
     *
     * @param nickname  the nickname
     * @param colorName the color name
     * @param cur       the cur
     * @throws GameAlreadyStartedException the game already started exception
     * @throws InvalidConnectionException  the invalid connection exception
     * @throws NicknameOfflineException    the nickname offline exception
     */
    public synchronized void connect(String nickname,String colorName, NetworkMode cur) throws GameAlreadyStartedException, InvalidConnectionException, NicknameOfflineException {
        System.out.println("[SERVER LOG] Received connection request from: " + nickname);

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
                throw new InvalidConnectionException("Nickname already in use by a connected player.");
            }
        }

        if (game.isGameStarted()) {
            throw new GameAlreadyStartedException("Game already started");
        }
        Color chosenColor;
        try {
            chosenColor = Color.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidConnectionException("Invalid or non-existent color: " + colorName);
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

            // 2 NETWORK RECORDING AND BROADCAST
            virtualView.registerClient(nickname, cur);
            System.out.println("[SERVER LOG] Player " + nickname + " successfully added to the board.");

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

            // We bounce the error back to the caller
            throw new InvalidConnectionException("Failed to complete connection for " + nickname + ": " + e.getMessage());
        }
    }

    /**
     * Sets expected players.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     */
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


    /**
     * Move totem.
     *
     * @param nickname    the nickname
     * @param offerTileId the offer tile id
     */
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

    /**
     * Add card.
     *
     * @param nickname the nickname
     * @param cardId   the card id
     */
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

    /**
     * Add extra card.
     *
     * @param nickname the nickname
     * @param cardId   the card id
     */
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

    /**
     * Skip extra draw.
     *
     * @param nickname the nickname
     */
    public synchronized void skipExtraDraw(String nickname) {
        addExtraCard(nickname, null);
    }

    /**
     * Handle disconnection.
     *
     * @param nickname the nickname
     */
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
            // Player not in game, disconnect ignored
            return;
        }

        // Guard: if already disconnected, ignore duplicate call
        // Prevent ping RMI and socket heartbeat from both triggering
        if (player.isDisconnected()) {
            System.out.println("[SERVER LOG] Duplicate logout ignored for: " + nickname);
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
        this.savedToDb = false;
        this.game = new Game(); // we create a new game so the server is now ready to start a new game
    }


    // for the advanced function resilience
    private void suspendPlayer(String nickname) {
        System.out.println("[SERVER LOG]: Suspension for logging out of: " + nickname + ". The game continues.");

        try {
            Player player = getPlayerByNickname(nickname);

           // 1. MARK AS DISCONNECTED FIRST (point of truth)
            player.setDisconnected(true);

            // 2. Remove the dead network channel
            if (virtualView != null) {
                virtualView.unregisterClient(nickname);
            }

            // 3.  pre-game: host dropout
            if (!game.isGameStarted()) {
                if (nickname.equals(game.getHostNickname())) {
                    handleHostDisconnection();
                }
                // Broadcast LOBBY update and STOP (there is no FSM to manage)
                if (virtualView != null) {
                    virtualView.broadcastUpdate(buildGameState());
                }
                return;
            }

            // 4. Game in progress: manage the skip of the turn
            if (game.getActivePlayer() != null
                    && game.getActivePlayer().getNickname().equals(nickname)) {
                // Player was ACTIVE: skip immediately via FSM
                advancePastDisconnectedPlayer();
            } else {
                // Player was in QUEUE: silently remove from structures
                advancePastQueuedDisconnectedPlayer(player);
            }

            // 5. Count players online AFTER the skip
            long onlineCount = game.getPlayers().stream()
                    .filter(p -> !p.isDisconnected())
                    .count();

            if (onlineCount == 0) {
                System.out.println("[RESILIENCE] Everyone offline. Game paused.");
            } else if (onlineCount == 1) {
                gamePaused = true;
                startLastPlayerTimer(nickname);
            } else {virtualView.broadcastError("Player " + nickname + " has disconnected.");}
            // onlineCount > 1: game continues normally

            // 6. Single FINAL Broadcast (avoids double notifications)
            if (virtualView != null) {
                virtualView.broadcastUpdate(buildGameState());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] suspendPlayer for " + nickname + ": " + e.getMessage());
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
                        System.out.println("[Resilience] Automatic phase, no skip required.");
                default ->
                        System.err.println("[Resilience] Unknown phase: " + phaseName);
            }
            // NO broadcastUpdate here — suspendPlayer() does after this return


        } catch (Exception e) {
            System.err.println("advancePastDisconnectedPlayer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void advancePastQueuedDisconnectedPlayer(Player player) {
        try {
            if (game.getCurrentPhase() == null) return;

            String phaseName = game.getCurrentPhase().getClass().getSimpleName();
            System.out.println("[Resilience] Dequeuing: " + player.getNickname() + " (phase: " + phaseName + ")");

            switch (phaseName) {
                case "PlaceTotemState" -> {
                    // PlaceTotemState knows how to manage an inactive player:
                    // - removes from placementOrder if present
                    // - moves the totem to TurnTile if necessary
                    // - DOES NOT advance the turn (leaves the activePlayer unchanged)
                    game.skipPlayerTurn(player);
                }
                case "AddCardState" -> {
                    // AddCardState removes the player from drawOrder
                    // and moves the totem, but does NOT call advanceTurn()
                    game.skipPlayerTurn(player);
                }
                case "ExtraDrawState" -> {
                    // ExtraDrawState removes from eligiblePlayers without advancing
                    game.skipPlayerTurn(player);
                }
                default -> {
                    // Automatic steps: no action necessary
                }
            }

            // NOTE: We do NOT do broadcastUpdate() here!
            // The final broadcast is handled by suspendPlayer() to avoid duplicates.

        } catch (Exception e) {
            System.err.println("Error in advancePastQueuedDisconnectedPlayer: " + e.getMessage());
        }
    }


    /**
     * Reconnect.
     *
     * @param nickname the nickname
     * @param cur      the cur
     */
    public synchronized void reconnect(String nickname, NetworkMode cur) {
        try {
            Player player = getPlayerByNickname(nickname);
            player.setDisconnected(false);
            System.out.println("[Resilience] " + nickname + " is back ONLINE.");
            //update network layer registration
            // (remove old RMI/Socket stub and register new one)
            virtualView.unregisterClient(nickname);
            virtualView.registerClient(nickname, cur);

            // Notify other players of reconnection
            try {
                virtualView.broadcastError("Player " + nickname + " has reconnected.");
            } catch (Exception e) {System.err.println("[SERVER LOG] Error notifying reconnection " + nickname + ": " + e.getMessage());}

            // Clear the 60s timer if it was running
            if (lastPlayerTimer != null && !lastPlayerTimer.isDone()) {
                lastPlayerTimer.cancel(false);
                lastPlayerTimer = null;
                System.out.println("[Resilience] Timer 60s cancellato.");
            }
            // The broadcastUpdate informs all clients including the returned one,
            // which at this point has already been registered by the calling network layer
            gamePaused = false;
            if (virtualView != null) {
                virtualView.broadcastUpdate(buildGameState());
            }

        } catch (Exception e) {
            System.err.println("Error in reconnect for " + nickname + ": " + e.getMessage());
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

        // 1. Immediate notification to remaining player
        try {
            if (virtualView != null) {
                virtualView.sendError(lastPlayer.getNickname(), "Player " + nickname + " has disconnected." + "You are the only player connected. Automatic win in 60 seconds if no one logs back in.");
            }
        } catch (Exception e) {
            System.err.println("[Resilience] Unable to notify last player: " + e.getMessage());
        }

        System.out.println("[Resilience]  60s timer started. Last player connected: " + lastPlayer.getNickname());

        // 2. Schedule the timer
        lastPlayerTimer = timerScheduler.schedule(() -> {
            synchronized (ServerController.this) {
                // Recalculate onlineCount when it expires (someone might have reconnected!)
                long stillOnline = game.getPlayers().stream()
                        .filter(p -> !p.isDisconnected())
                        .count();

                if (stillOnline == 1) {
                    System.out.println("[Resilience] ⏰ Timer scaduto. Dichiarazione vincitore per abbandono.");

                    try {
                        //Calculate final scores BEFORE building state
                        game.countFinalPoints();
                        game.forceGameOver();
                        saveGameResults();

                        //Build the GameState AFTER countFinalPoints and forceGameOver
                        // Make sure GameState.PlayerState copies isDisconnected() from the Player!
                        GameState finalState = buildGameState();
                        attachLeaderboardToGameState(finalState);

                        // CRITICAL 3: Use broadcastWinner (not broadcastUpdate) to signal the end
                        if (virtualView != null) {
                            virtualView.broadcastWinner(finalState);
                        }

                    } catch (Exception e) {
                        System.err.println("Error during broadcastWinner from timer: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("[Resilience] Timer expired but someone reconnected (" + stillOnline + " online). No automatic winners.");
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

    /**
     * Build game state game state.
     *
     * @return the game state
     */
/*
     Builds a serializable GameState snapshot from the current Game.
     This object is sent to clients through the network.
     */
    public GameState buildGameState() {
        GameState gs = new GameState(game);
        //game paused or not
        gs.setGamePaused(gamePaused);
        if (game.isFinalPointsCounted()) {
            attachLeaderboardToGameState(gs);
        }
        return gs;
    }

    private void attachLeaderboardToGameState(GameState state) {
        if (!state.isGameOver() || !game.isFinalPointsCounted()) {
            return;
        }

        int numPlayers = game.getPlayers().size();
        List<LeaderboardEntry> leaderboard = gameResultDAO.getLeaderboard(numPlayers);
        state.setFinalLeaderboard(leaderboard);

        if (!leaderboard.isEmpty() && !game.getWinner().isEmpty()) {
            String winner = game.getWinner().getFirst().getNickname();
            int rank = gameResultDAO.getPlayerPosition(winner, numPlayers);
            state.setPlayerRankInLeaderboard(rank);
        }
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
            System.out.println("[SERVER LOG]: Failed to send error to " + nickname + ". Disconnecting...");
            handleDisconnection(nickname);
        }
    }


    /**
     * Gets available colors.
     *
     * @return the available colors
     * @throws Exception the exception
     */
    public List<Color> getAvailableColors() throws Exception {
        return game.getAvailableColors();
    }

    /**
     * Gets leaderboard.
     *
     * @param numPlayers the num players
     * @return the leaderboard
     */
    public List<LeaderboardEntry> getLeaderboard(int numPlayers) {
        return gameResultDAO.getLeaderboard(numPlayers);
    }

    /**
     * Gets player position.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     * @return the player position
     */
    public int getPlayerPosition(String nickname, int numPlayers) {
        return gameResultDAO.getPlayerPosition(nickname, numPlayers);
    }

    /**
     * Save game results.
     */
// ADD method to save results at the end of the game
    public void saveGameResults() {
        System.out.println("[DB] saveGameResults() chiamato — savedToDb: " + savedToDb);
        Map<Player, Integer> ranking = game.getFinalRanking();
        System.out.println("[DB] ranking size: " + ranking.size());

        if (ranking.isEmpty()){
            System.out.println("[DB] empty ranking, nothing to save");
            return; // no players connected, nothing to save
        }

        List<String> nicknames  = new ArrayList<>();
        List<Integer> scores    = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        for (Player player : ranking.keySet()) {
            // 1. Let's immediately retrieve the position from the Map
            int position = ranking.get(player);

            // 2. We print using the 'player' object directly (and no longer 'entry')
            System.out.println("[DB]   " + player.getNickname()
                    + " | PP: " + player.getPP()
                    + " | Food: " + player.getFood()
                    + " | Posizione: " + position);;
            nicknames.add(player.getNickname());
            scores.add(player.getPP());
            positions.add(position);
        }

        gameResultDAO.saveGameResults(game.getPlayers().size(), nicknames, scores, positions);
        savedToDb = true;
    }
}