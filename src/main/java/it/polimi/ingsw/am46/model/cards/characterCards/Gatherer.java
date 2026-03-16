package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Gatherer extends CharacterCard {
    private final int discount;

    public Gatherer(int id, int era, int cost, int discount){
        super(id, era, cost, SubType.GATHERER);
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }

    @Override
    public SubType getSubType(){
        return SubType.GATHERER;
    }

}
