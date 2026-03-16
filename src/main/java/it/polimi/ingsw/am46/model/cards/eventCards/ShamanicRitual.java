package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class ShamanicRitual extends EventCard {
    public ShamanicRitual(int id, int era, int cost) {
        super(id, era, cost, SubType.SHR);
    }

    @Override
    public void resolve(Game game) {
        //logica da implementare
    }

    @Override
    public SubType getSubType() {
        return SubType.SHR;
    }
}
