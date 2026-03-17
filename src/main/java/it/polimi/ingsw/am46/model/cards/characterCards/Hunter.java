package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Hunter extends CharacterCard {
    private final boolean food;

    public Hunter(int id, int era, int cost, boolean food, int minPlayers) {
        super(id, era, cost, SubType.HUNTER, minPlayers == 0 ? 2 : minPlayers);
        this.food = food;
    }

    public boolean isFood() {
        return food;
    }

    @Override
    public SubType getSubType() {
        return SubType.HUNTER;
    }

    @Override
    public void applyEffect(Player player) {
        //logica da implementare
    }
}
