package it.polimi.ingsw.am46.server.model.cards.characterCards;

import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.enums.Item;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

import java.util.Optional;

/**
 * The type Character card.
 */
public abstract class CharacterCard extends TribeCard {
    private final int minPlayers;

    /**
     * Instantiates a new Character card.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param subType    the sub type
     * @param minPlayers the min players
     */
    public CharacterCard(int id, int era, int cost, SubType subType, int minPlayers) {
        super(id, era, cost, Type.CHARACTER, subType);
        this.minPlayers = minPlayers;
    }


    /**
     * Gets stars.
     *
     * @return the stars
     */
// POLYMORPHIC GETTERS
    public int getStars() { return 0; }

    /**
     * Gets pp.
     *
     * @return the pp
     */
    public int getPp() { return 0; }

    /**
     * Gets discount.
     *
     * @return the discount
     */
    public int getDiscount() { return 0; }

    /**
     * Is food boolean.
     *
     * @return the boolean
     */
    public boolean isFood() { return false; }

    /**
     * Gets item.
     *
     * @return the item
     */
    public Optional<Item> getItem() {
        return Optional.empty();
    }

    /**
     * Gets min players.
     *
     * @return the min players
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    public Type getType() {
        return Type.CHARACTER;
    }

    //POLYMORPHIC ADD
    @Override
    public void addToPlayer(Player player) {
        player.addCard(this);
    }

}
