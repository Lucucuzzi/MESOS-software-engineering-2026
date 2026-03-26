package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;

import java.util.ArrayList;

public interface GameContext {
    Player getActivePlayer();
    ArrayList<Player> getPlayers();
    Board getBoard();
    EventCard getCurrentEvent();
}
