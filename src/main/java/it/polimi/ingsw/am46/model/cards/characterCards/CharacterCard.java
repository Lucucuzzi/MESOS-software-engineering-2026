package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public class CharacterCard extends TribeCard {
    private final int minPlayers;

    public CharacterCard(int id, int era, int cost, SubType subType, int minPlayers) {
        super(id, era, cost, Type.CHARACTER, subType);
        this.minPlayers = minPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public Type getType() {
        return Type.CHARACTER;
    }
}
