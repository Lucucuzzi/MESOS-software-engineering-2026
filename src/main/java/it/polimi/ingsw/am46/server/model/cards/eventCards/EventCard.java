package it.polimi.ingsw.am46.server.model.cards.eventCards;

import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

public abstract class EventCard extends TribeCard {
    public final boolean finalEvent;

    public EventCard(int id, int era, int cost, SubType subType, boolean finalEvent) {
        super(id, era, cost, Type.EVENT, subType);
        this.finalEvent = finalEvent;
    }
    public Type getType() {
        return Type.EVENT;
    }

    public boolean isFinalEvent() {
        return finalEvent;
    }
}
