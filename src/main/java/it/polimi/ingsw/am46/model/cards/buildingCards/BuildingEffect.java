package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.GameContext;

@FunctionalInterface
public interface BuildingEffect {
    void apply(GameContext context);
}

