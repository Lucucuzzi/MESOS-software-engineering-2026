package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.List;

public interface GameContext {
    Player getActivePlayer();
    ArrayList<Player> getPlayers();
    Board getBoard();
    EventCard getCurrentEvent();
    void setCurrentPhase(RoundPhase phase);
    void setActivePlayer(Player player);

    //Game state
    int getRound();
    int getCurrentEra();

    void countFinalPoints();
    List<Player> getWinner();
    boolean isGameOver();
    void resolveRound();
    void setCurrentEvent(EventCard event);
    RoundPhase getCurrentPhase();
    public List<Integer> getRecentlyResolvedEvents();

    public void setRecentlyResolvedEvents(List<Integer> recentlyResolvedEvents);
}
