package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

/**
 * The type Gatherer.
 */
public class Gatherer extends CharacterCard {


    /**
     * Instantiates a new Gatherer.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param minPlayers the min players
     */
    public Gatherer(int id, int era, int cost, int minPlayers) {
        super(id, era, cost, SubType.GATHERER, minPlayers == 0 ? 2 : minPlayers);
    }

    @Override
    public SubType getSubType(){
        return SubType.GATHERER;
    }

}
