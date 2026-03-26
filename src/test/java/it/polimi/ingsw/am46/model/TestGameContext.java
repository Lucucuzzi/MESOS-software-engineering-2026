package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;

import java.util.ArrayList;
import java.util.List;

public class TestGameContext implements GameContext {
    private final ArrayList<Player> players;
    private Player currentPlayer;
    private Board board;
    private EventCard currentEvent;

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
    public void setCurrentEvent(EventCard event) {
        this.currentEvent = event;
    }
}
