package it.polimi.ingsw.am46.model.cards;

import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public abstract class TribeCard extends Card {
    private final SubType subType;


    public TribeCard(int id, int era, int cost, Type type, SubType subType) {
        super(id, era, cost, type);
        this.subType = subType;
    }

    public SubType getSubType() {
        return subType;
    }

}
