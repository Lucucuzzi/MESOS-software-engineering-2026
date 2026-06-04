package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

public class Artist extends CharacterCard {
    public Artist(int id, int era, int cost, int minPlayers){
       super(id, era, cost, SubType.ARTIST,minPlayers == 0 ? 2 : minPlayers);
    }

    @Override
    public SubType getSubType() {
        return SubType.ARTIST;
    }
}
