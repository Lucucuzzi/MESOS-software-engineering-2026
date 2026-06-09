package it.polimi.ingsw.am46.server.model.cards.characterCards;


import it.polimi.ingsw.am46.server.model.cards.enums.Item;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

import java.util.Optional;

/**
 * The type Inventor.
 */
public class Inventor extends CharacterCard {
    private final Item item;

    /**
     * Instantiates a new Inventor.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param item       the item
     * @param minPlayers the min players
     */
    public Inventor(int id, int era, int cost, Item item, int minPlayers) {
        super(id, era, cost, SubType.INVENTOR, minPlayers == 0 ? 2 : minPlayers);
        this.item = item;
    }

    @Override
    public Optional<Item> getItem() {
        return Optional.of(this.item);
    }

    @Override
    public SubType getSubType() {
        return SubType.INVENTOR;
    }
}
