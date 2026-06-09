package it.polimi.ingsw.am46.server.model;

import it.polimi.ingsw.am46.server.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.server.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.List;

/**
 * The interface Game context.
 */
public interface GameContext {
    /**
     * Gets active player.
     *
     * @return the active player
     */
    Player getActivePlayer();

    /**
     * Gets players.
     *
     * @return the players
     */
    ArrayList<Player> getPlayers();

    /**
     * Gets board.
     *
     * @return the board
     */
    Board getBoard();

    /**
     * Gets current event.
     *
     * @return the current event
     */
    EventCard getCurrentEvent();

    /**
     * Sets current phase.
     *
     * @param phase the phase
     */
    void setCurrentPhase(RoundPhase phase);

    /**
     * Sets active player.
     *
     * @param player the player
     */
    void setActivePlayer(Player player);

    /**
     * Gets round.
     *
     * @return the round
     */
//Game state
    int getRound();

    /**
     * Gets current era.
     *
     * @return the current era
     */
    int getCurrentEra();

    /**
     * Count final points.
     */
    void countFinalPoints();

    /**
     * Gets winner.
     *
     * @return the winner
     */
    List<Player> getWinner();

    /**
     * Is game over boolean.
     *
     * @return the boolean
     */
    boolean isGameOver();

    /**
     * Resolve round.
     */
    void resolveRound();

    /**
     * Sets current event.
     *
     * @param event the event
     */
    void setCurrentEvent(EventCard event);

    /**
     * Gets current phase.
     *
     * @return the current phase
     */
    RoundPhase getCurrentPhase();

    /**
     * Gets recently resolved events.
     *
     * @return the recently resolved events
     */
    public List<Integer> getRecentlyResolvedEvents();

    /**
     * Sets recently resolved events.
     *
     * @param recentlyResolvedEvents the recently resolved events
     */
    public void setRecentlyResolvedEvents(List<Integer> recentlyResolvedEvents);
}
