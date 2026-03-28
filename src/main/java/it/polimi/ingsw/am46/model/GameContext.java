package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.model.state.RoundPhase;

import java.util.ArrayList;

public interface GameContext {
    Player getActivePlayer();
    ArrayList<Player> getPlayers();
    Board getBoard();
    EventCard getCurrentEvent();
    void setCurrentPhase(RoundPhase phase);
    void setActivePlayer(Player player);
}
