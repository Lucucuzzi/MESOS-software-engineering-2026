package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public abstract class BuildingCard extends Card {
    private final int pp, food;

    public BuildingCard(int id, int era, int cost, int pp, int food) {
        super(id, era, cost, Type.BUILDING);
        this.pp = pp;
        this.food = food;
    }
    public int getPp() {
        return pp;
    }
    public int getFood() {
        return food;
    }

    public Type getType() {
        return Type.BUILDING;
    }

}
