package it.polimi.ingsw.am46.model;

import java.util.ArrayList;

public interface GameContext {
    Player getCurrentPlayer();
    ArrayList<Player> getPlayers();
}
