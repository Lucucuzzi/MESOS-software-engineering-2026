package it.polimi.ingsw.am46.server.model.cards;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

/**
 * The type Card.
 */
public abstract class Card {
    private final int id, era, cost;
    private final Type type;

    /**
     * Instantiates a new Card.
     *
     * @param id   the id
     * @param era  the era
     * @param cost the cost
     * @param type the type
     */
    public Card(int id, int era, int cost, Type type) {
        this.id = id;
        this.era = era;
        this.cost = cost;
        this.type = type;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets era.
     *
     * @return the era
     */
    public int getEra() {
        return era;
    }

    /**
     * Gets cost.
     *
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Add to player.
     *
     * @param player the player
     */
    public void addToPlayer(Player player) {}

    /**
     * Apply effect.
     *
     * @param player the player
     */
    public void applyEffect(Player player) {}

    /**
     * Resolve.
     *
     * @param gameContext the game context
     */
    public void resolve(GameContext gameContext) {}






}
