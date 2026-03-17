package it.polimi.ingsw.am46.model.cards.characterCards;


import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Inventor extends CharacterCard {
    private final Item item;

    public Inventor(int id, int era, int cost, Item item, int minPlayers) {
        super(id, era, cost, SubType.INVENTOR, minPlayers == 0 ? 2 : minPlayers);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public SubType getSubType() {
        return SubType.INVENTOR;
    }
}
