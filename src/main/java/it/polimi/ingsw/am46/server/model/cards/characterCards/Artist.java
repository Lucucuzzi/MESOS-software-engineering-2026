package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

/**
 * The type Artist.
 */
public class Artist extends CharacterCard {
    /**
     * Instantiates a new Artist.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param minPlayers the min players
     */
    public Artist(int id, int era, int cost, int minPlayers){
       super(id, era, cost, SubType.ARTIST,minPlayers == 0 ? 2 : minPlayers);
    }

    @Override
    public SubType getSubType() {
        return SubType.ARTIST;
    }
}
