package it.polimi.ingsw.am46.network.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.Card;


/**
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


    private final List<Integer> topRowCardIds;
    private final List<Integer> bottomRowCardIds;

    private final List<PlayerState> playerStates;

    private final List<OfferTileState> offerTileStates;

    // Winners (populated only at the end of the game)
    private final List<String> winners;

    // Context message (e.g., “New Era!”, “Round 5”)
    private final String contextMessage;



    public GameState(Game game) {
        this.round = game.getRound();
        this.currentEra = game.getCurrentEra();
        this.currentPhaseName = game.getCurrentPhase().getClass().getSimpleName();
        this.activePlayerNickname = game.getActivePlayer().getNickname();
        this.isGameOver = game.isGameOver();
        this.winners = new ArrayList<>();
        this.topRowCardIds = game.getBoard().getTopRow().stream().map(Card::getId).toList();
        this.bottomRowCardIds = game.getBoard().getBottomRow().stream().map(Card::getId).toList();
        this.playerStates = new ArrayList<>();
        for(Player player : game.getPlayers()) {
            this.playerStates.add(new PlayerState(player));
        }
        this.offerTileStates = new ArrayList<>();
        for(OfferTile tile : game.getBoard().getOfferTiles()) {
            this.offerTileStates.add(new OfferTileState(tile));
        }
        this.contextMessage = "";

    }
    public int getRound() { return round; }
    public int getCurrentEra() { return currentEra; }
    public String getCurrentPhaseName() { return currentPhaseName; }
    public String getActivePlayerNickname() {
        return activePlayerNickname; }
    public boolean isGameOver() { return isGameOver; }
    public List<Integer> getTopRowCardIds() {
        return topRowCardIds; }
    public List<Integer> getBottomRowCardIds() {
        return bottomRowCardIds; }
    public List<PlayerState> getPlayerStates() {
        return playerStates; }
    public List<String> getWinners() { return winners; }
    public String getContextMessage() { return contextMessage; }


}
