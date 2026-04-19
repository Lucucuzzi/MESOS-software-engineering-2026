package it.polimi.ingsw.am46.network.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.Card;


/*
 * Data Transfer Object — a snapshot of the game's state.
 * It must be serializable to be transmitted over the network via RMI.
 * It contains only raw data (no logic, no references
 * to the actual game) — it is an immutable snapshot of the state.
 * The LocalModel on the client is updated with this object.
 * The View reads from this object to update itself.
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


    private final List<Integer> topRowCardIds;
    private final List<Integer> bottomRowCardIds;

    private final List<PlayerState> playerStates;

    private final List<OfferTileState> offerTileStates;

    // Winners (populated only at the end of the game)
    private final List<String> winners;

    // Context message (e.g., “New Era!”, “Round 5”)
    private final String contextMessage;



    public GameState(Game game) {
        this.gameStarted = game.isGameStarted();
        this.hostNickname = game.getHostNickname();
        this.expectedPlayers = game.getExpectedPlayers();
        this.connectedPlayers = game.getPlayers().size();
        this.round = gameStarted ? game.getRound() : 0;
        this.currentEra = gameStarted ? game.getCurrentEra() : 0;
        this.currentPhaseName = gameStarted && game.getCurrentPhase() != null
                ? game.getCurrentPhase().getClass().getSimpleName()
                : "Lobby";
        this.activePlayerNickname = gameStarted && game.getActivePlayer() != null
                ? game.getActivePlayer().getNickname()
                : null;
        this.isGameOver = gameStarted && game.isGameOver();
        this.winners = new ArrayList<>();
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
    public int getRound() { return round; }
    public int getCurrentEra() { return currentEra; }
    public String getCurrentPhaseName() { return currentPhaseName; }
    public String getActivePlayerNickname() {
        return activePlayerNickname; }
    public boolean isGameOver() { return isGameOver; }
    public List<Integer> getTopRowCardIds() {
        return new ArrayList<>(topRowCardIds); }
    public List<Integer> getBottomRowCardIds() {
        return new ArrayList<>(bottomRowCardIds) ; }
    public List<PlayerState> getPlayerStates() {
        return new ArrayList<>(playerStates); }
    public List<String> getWinners() {
        return new ArrayList<>(winners); }
    public String getContextMessage() { return contextMessage; }
    public boolean isGameStarted() { return gameStarted; }
    public String getHostNickname() { return hostNickname; }
    public Integer getExpectedPlayers() { return expectedPlayers; }
    public int getConnectedPlayers() { return connectedPlayers; }

    public List<OfferTileState> getOfferTileStates() {
        return new ArrayList<>(offerTileStates);
    }
}
