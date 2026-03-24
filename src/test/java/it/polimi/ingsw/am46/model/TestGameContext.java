package it.polimi.ingsw.am46.model;

import java.util.ArrayList;
import java.util.List;

public class TestGameContext implements GameContext {
    private final ArrayList<Player> players;
    private Player currentPlayer;

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
}
