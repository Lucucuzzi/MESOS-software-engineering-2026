package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public class EventCard extends TribeCard {
    public EventCard(int id, int era, int cost, SubType subType) {
        super(id, era, cost, Type.EVENT, subType);
    }
    public Type getType() {
        return Type.EVENT;
    }
}
