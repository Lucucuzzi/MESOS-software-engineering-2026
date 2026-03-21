package it.polimi.ingsw.am46.model;

import java.util.ArrayList;

public interface GameContext {
    Player getActivePlayer();
    ArrayList<Player> getPlayers();
}
