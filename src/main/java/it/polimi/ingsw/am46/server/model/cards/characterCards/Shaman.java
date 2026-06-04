package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

public class Shaman extends CharacterCard {
    private final int stars;

    public Shaman(int id, int era, int cost, int stars, int minPlayers) {
        super(id, era, cost, SubType.SHAMAN, minPlayers == 0 ? 2 : minPlayers);
        this.stars = stars;
    }

    @Override
    public int getStars() {
        return this.stars;
    }

    @Override
    public SubType getSubType(){
        return SubType.SHAMAN;
    }
}
