package it.polimi.ingsw.am46.server.model.cards;

import it.polimi.ingsw.am46.server.model.cards.enums.SubType;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

/**
 * The type Tribe card.
 */
public abstract class TribeCard extends Card {
    private final SubType subType;


    /**
     * Instantiates a new Tribe card.
     *
     * @param id      the id
     * @param era     the era
     * @param cost    the cost
     * @param type    the type
     * @param subType the sub type
     */
    public TribeCard(int id, int era, int cost, Type type, SubType subType) {
        super(id, era, cost, type);
        this.subType = subType;
    }

    /**
     * Gets sub type.
     *
     * @return the sub type
     */
    public SubType getSubType() {
        return subType;
    }

}
