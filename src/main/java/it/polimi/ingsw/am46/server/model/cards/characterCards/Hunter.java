package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

public class Hunter extends CharacterCard {
    private final boolean food;

    public Hunter(int id, int era, int cost, boolean food, int minPlayers) {
        super(id, era, cost, SubType.HUNTER, minPlayers == 0 ? 2 : minPlayers);
        this.food = food;
    }

    @Override
    public boolean isFood() {
        return this.food;
    }

    @Override
    public SubType getSubType() {
        return SubType.HUNTER;
    }

    @Override
    public void applyEffect(Player player) {
        if(isFood()) {
            int bonus = player.countCharactersByType(SubType.HUNTER);
            player.modifyFood(bonus);
        }
    }
}
