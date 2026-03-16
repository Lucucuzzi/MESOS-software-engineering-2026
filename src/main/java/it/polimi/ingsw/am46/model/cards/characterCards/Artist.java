package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public class Artist extends CharacterCard {
    public Artist(int id, int era, int cost){
       super(id, era, cost, SubType.ARTIST);
    }

    @Override
    public SubType getSubType() {
        return SubType.ARTIST;
    }
}
