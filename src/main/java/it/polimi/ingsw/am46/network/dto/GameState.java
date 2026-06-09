package it.polimi.ingsw.am46.network.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.am46.server.model.Game;
import it.polimi.ingsw.am46.server.model.OfferTile;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.Space;
import it.polimi.ingsw.am46.server.model.cards.Card;


/*
 * Data Transfer Object — a snapshot of the game's state.
 * It must be serializable to be transmitted over the network via RMI.
 * It contains only raw data (no logic, no references
 * to the actual game) — it is an immutable snapshot of the state.
 * The LocalModel on the client is updated with this object.
 * The View reads from this object to update itself.
 */

/**
 * The type Game state.
 */
public class GameState implements Serializable {
    // Java serialization version
    private static final long serialVersionUID = 1L;

    // General game state
    private final int round;
    private final int currentEra;
    private final String currentPhaseName;
    private final String activePlayerNickname;
    private final boolean isGameOver;
    private final boolean gameStarted;
    private final String hostNickname;
    private final Integer expectedPlayers;
    private final int connectedPlayers;
    private final List<String> availableColors;
    private List<String> turnOrder;
    private List<String> turnOrderWithGaps;
    private boolean finalPointsCounted;

    private boolean gamePaused;

    private final List<Integer> topRowCardIds;
    private final List<Integer> bottomRowCardIds;

    private final List<PlayerState> playerStates;

    private final List<OfferTileState> offerTileStates;

    // Winners (populated only at the end of the game)
    private final List<String> winners;

    // Context message (e.g., “New Era!”, “Round 5”)
    private final String contextMessage;
    private List<Integer> recentlyResolvedEvents = new ArrayList<>();

    private List<LeaderboardEntry> finalLeaderboard;
    private int playerRankInLeaderboard = -1;


    /**
     * Instantiates a new Game state.
     *
     * @param game the game
     */
    public GameState(Game game) {
        this.gameStarted = game.isGameStarted();
        this.turnOrder = new ArrayList<>();
        this.availableColors = game.getAvailableColors().stream().map(Enum::name).toList();
        this.hostNickname = game.getHostNickname();
        this.expectedPlayers = game.getExpectedPlayers();
        this.connectedPlayers = game.getPlayers().size();
        this.finalPointsCounted = game.isFinalPointsCounted();
        this.round = gameStarted ? game.getRound() : 0;
        this.recentlyResolvedEvents = game.getRecentlyResolvedEvents();
        this.currentEra = gameStarted ? game.getCurrentEra() : 0;
        this.currentPhaseName = gameStarted && game.getCurrentPhase() != null
                ? game.getCurrentPhase().getClass().getSimpleName()
                : "Lobby";
        this.activePlayerNickname = gameStarted && game.getActivePlayer() != null
               ? game.getActivePlayer().getNickname()
                : null;
        this.isGameOver = gameStarted && game.isGameOver();
        this.winners = new ArrayList<>();
        if (this.isGameOver) {
            this.finalLeaderboard = new ArrayList<>();
            for (Player winner : game.getWinner()) {
                this.winners.add(winner.getNickname());
            }
        } else if (game.isFinalPointsCounted()) {
            for (Player winner : game.getWinner()) {
                this.winners.add(winner.getNickname());
            }
        }
        this.playerStates = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            this.playerStates.add(new PlayerState(player));
        }




        if (gameStarted) {
            this.topRowCardIds = game.getBoard().getTopRow().stream().map(Card::getId).toList();
            this.bottomRowCardIds = game.getBoard().getBottomRow().stream().map(Card::getId).toList();
            this.offerTileStates = new ArrayList<>();
            for (OfferTile tile : game.getBoard().getOfferTiles()) {
                this.offerTileStates.add(new OfferTileState(tile));
            }
            for (Player player : game.getBoard().getTurnTile().getTurnOrder()) {
                this.turnOrder.add(player.getNickname());
            }
            this.turnOrderWithGaps = new ArrayList<>();
            for (Space s : game.getBoard().getTurnTile().getSpaces()) {
                Player p = s.getPlayer();
                this.turnOrderWithGaps.add(p != null ? p.getNickname() : null);
            }
            this.contextMessage = "";
        } else {
            this.topRowCardIds = new ArrayList<>();
            this.bottomRowCardIds = new ArrayList<>();
            this.offerTileStates = new ArrayList<>();
            if (expectedPlayers == null) {
                this.contextMessage = "Waiting for the host to choose the number of players";
            } else {
                this.contextMessage = "Waiting for players: " + connectedPlayers + "/" + expectedPlayers;
            }
        }


    }

    /**
     * Gets round.
     *
     * @return the round
     */
    public int getRound() { return round; }

    /**
     * Gets current era.
     *
     * @return the current era
     */
    public int getCurrentEra() { return currentEra; }

    /**
     * Gets current phase name.
     *
     * @return the current phase name
     */
    public String getCurrentPhaseName() { return currentPhaseName; }

    /**
     * Gets active player nickname.
     *
     * @return the active player nickname
     */
    public String getActivePlayerNickname() {
        return activePlayerNickname; }

    /**
     * Is game over boolean.
     *
     * @return the boolean
     */
    public boolean isGameOver() { return isGameOver; }

    /**
     * Gets top row card ids.
     *
     * @return the top row card ids
     */
    public List<Integer> getTopRowCardIds() {
        return new ArrayList<>(topRowCardIds); }

    /**
     * Gets bottom row card ids.
     *
     * @return the bottom row card ids
     */
    public List<Integer> getBottomRowCardIds() {
        return new ArrayList<>(bottomRowCardIds) ; }

    /**
     * Gets player states.
     *
     * @return the player states
     */
    public List<PlayerState> getPlayerStates() {
        return new ArrayList<>(playerStates); }

    /**
     * Gets winners.
     *
     * @return the winners
     */
    public List<String> getWinners() {
        return new ArrayList<>(winners); }

    /**
     * Gets context message.
     *
     * @return the context message
     */
    public String getContextMessage() { return contextMessage; }

    /**
     * Is game started boolean.
     *
     * @return the boolean
     */
    public boolean isGameStarted() { return gameStarted; }

    /**
     * Gets host nickname.
     *
     * @return the host nickname
     */
    public String getHostNickname() { return hostNickname; }

    /**
     * Gets expected players.
     *
     * @return the expected players
     */
    public Integer getExpectedPlayers() { return expectedPlayers; }

    /**
     * Gets connected players.
     *
     * @return the connected players
     */
    public int getConnectedPlayers() { return connectedPlayers; }

    /**
     * Gets available colors.
     *
     * @return the available colors
     */
    public List<String> getAvailableColors() {
        return availableColors;
    }

    /**
     * Gets offer tile states.
     *
     * @return the offer tile states
     */
    public List<OfferTileState> getOfferTileStates() {
        return new ArrayList<>(offerTileStates);
    }

    /**
     * Gets turn order.
     *
     * @return the turn order
     */
    public List<String> getTurnOrder() {
        return turnOrder;
    }

    /**
     * Is final points counted boolean.
     *
     * @return the boolean
     */
    public boolean isFinalPointsCounted() {
        return finalPointsCounted;
    }

    /**
     * Gets recently resolved events.
     *
     * @return the recently resolved events
     */
    public List<Integer> getRecentlyResolvedEvents() {
        return recentlyResolvedEvents;
    }

    /**
     * Gets player state by nickname.
     *
     * @param nickname the nickname
     * @return the player state by nickname
     */
    public PlayerState getPlayerStateByNickname(String nickname) {
        for (PlayerState ps : playerStates) {
            if (ps.getNickname().equals(nickname)) {
                return ps;
            }
        }
        return null;
    }

    /**
     * Gets turn order with gaps.
     *
     * @return the turn order with gaps
     */
    public List<String> getTurnOrderWithGaps() {
        return turnOrderWithGaps;
    }

    /**
     * Is game paused boolean.
     *
     * @return the boolean
     */
    public boolean isGamePaused() { return gamePaused; }

    /**
     * Sets game paused.
     *
     * @param v the v
     */
    public void setGamePaused(boolean v) { this.gamePaused = v; }

    /**
     * Gets final leaderboard.
     *
     * @return the final leaderboard
     */
    public List<LeaderboardEntry> getFinalLeaderboard() {
        return finalLeaderboard != null ? finalLeaderboard : new ArrayList<>();
    }

    /**
     * Sets final leaderboard.
     *
     * @param leaderboard the leaderboard
     */
    public void setFinalLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.finalLeaderboard = leaderboard;
    }

    /**
     * Gets player rank in leaderboard.
     *
     * @return the player rank in leaderboard
     */
    public int getPlayerRankInLeaderboard() {
        return playerRankInLeaderboard;
    }

    /**
     * Sets player rank in leaderboard.
     *
     * @param rank the rank
     */
    public void setPlayerRankInLeaderboard(int rank) {
        this.playerRankInLeaderboard = rank;
    }

    /**
     * Gets num players in game.
     *
     * @return the num players in game
     */
    public int getNumPlayersInGame() {
        return connectedPlayers;
    }
}
