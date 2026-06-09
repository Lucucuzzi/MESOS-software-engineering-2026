package it.polimi.ingsw.am46.server.model.cards.eventCards;

import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

/**
 * The type Event card.
 */
public abstract class EventCard extends TribeCard {
    /**
     * The Final event.
     */
    public final boolean finalEvent;

    /**
     * Instantiates a new Event card.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param subType    the sub type
     * @param finalEvent the final event
     */
    public EventCard(int id, int era, int cost, SubType subType, boolean finalEvent) {
        super(id, era, cost, Type.EVENT, subType);
        this.finalEvent = finalEvent;
    }
    public Type getType() {
        return Type.EVENT;
    }

    /**
     * Is final event boolean.
     *
     * @return the boolean
     */
    public boolean isFinalEvent() {
        return finalEvent;
    }
}
