package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.server.model.Board;
import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.server.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.List;

public class TestGameContext implements GameContext {
    private final ArrayList<Player> players;
    private Player currentPlayer;
    private Board board;
    private EventCard currentEvent;
    private RoundPhase currentPhase;

    public TestGameContext(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    @Override
    public Player getActivePlayer() {
        return currentPlayer;
    }

    @Override
    public ArrayList<Player> getPlayers() {
        return players;
    }
    @Override
    public Board getBoard() {
        return board;
    }
    public void setBoard(Board board) {
        this.board = board;
    }
    @Override
    public EventCard getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void setActivePlayer(Player player) {
        this.currentPlayer = player;
    }

    @Override
    public int getRound() {
        return 0;
    }

    @Override
    public int getCurrentEra() {
        return 0;
    }

    @Override
    public void countFinalPoints() {

    }

    @Override
    public List<Player> getWinner() {
        return null;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

    @Override
    public void resolveRound() {
        return;
    }


    @Override
    public void setCurrentPhase(RoundPhase phase) {
        // Add this too if missing, for the final test check
        this.currentPhase = phase;
    }

    public RoundPhase getCurrentPhase() {
        return currentPhase;
    }

    @Override
    public List<Integer> getRecentlyResolvedEvents() {
        return List.of();
    }

    @Override
    public void setRecentlyResolvedEvents(List<Integer> recentlyResolvedEvents) {

    }

    public void setCurrentEvent(EventCard event) {
        this.currentEvent = event;
    }
}
