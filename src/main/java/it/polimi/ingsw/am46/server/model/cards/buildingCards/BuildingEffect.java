package it.polimi.ingsw.am46.server.model.cards.buildingCards;

import it.polimi.ingsw.am46.server.model.GameContext;

/**
 * The interface Building effect.
 */
@FunctionalInterface
public interface BuildingEffect {
    /**
     * Apply.
     *
     * @param context the context
     */
    void apply(GameContext context);
}

