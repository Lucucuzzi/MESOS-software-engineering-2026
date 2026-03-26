package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Builder extends CharacterCard {
    private final int pp, discount;
    public Builder(int id, int era, int cost, int pp, int discount, int minPlayers) {
        super(id, era, cost, SubType.BUILDER,minPlayers == 0 ? 2 : minPlayers);
        this.pp = pp;
        this.discount = discount;
    }

    @Override
    public int getPp() {
        return this.pp;
    }
    @Override
    public int getDiscount() {
        return this.discount;
    }
    @Override
    public SubType getSubType() {
        return SubType.BUILDER;
    }

}
