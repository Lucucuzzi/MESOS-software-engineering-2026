package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Hunt extends EventCard {

    public Hunt(int id, int era, int cost, boolean finalEvent) {
        super(id, era, cost, SubType.HUNT, finalEvent);
    }

    @Override
    public void resolve(Game game) {
        //logica da implementare
    }

    @Override
    public SubType getSubType() {
        return SubType.HUNT;
    }
}
