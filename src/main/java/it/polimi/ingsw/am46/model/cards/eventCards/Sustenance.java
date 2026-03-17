package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Sustenance extends EventCard {
    public Sustenance(int id, int era, int cost, boolean finalEvent) {
        super(id, era, cost, SubType.SUSTENANCE, finalEvent);
    }

    @Override
    public void resolve(Game game) {
        //logica da implementare
    }

    @Override
    public SubType getSubType() {
        return SubType.SUSTENANCE;
    }
}
