package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;

import java.util.Optional;

public class CharacterCard extends TribeCard {
    private final int minPlayers;

    public CharacterCard(int id, int era, int cost, SubType subType, int minPlayers) {
        super(id, era, cost, Type.CHARACTER, subType);
        this.minPlayers = minPlayers;
    }



    // POLYMORPHIC GETTERS
    public int getStars() { return 0; }
    public int getPp() { return 0; }
    public int getDiscount() { return 0; }
    public boolean isFood() { return false; }
    public Optional<Item> getItem() {
        return Optional.empty();
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public Type getType() {
        return Type.CHARACTER;
    }

}
