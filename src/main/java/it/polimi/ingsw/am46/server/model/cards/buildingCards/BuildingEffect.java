package it.polimi.ingsw.am46.server.model.cards.buildingCards;

import it.polimi.ingsw.am46.server.model.GameContext;

@FunctionalInterface
public interface BuildingEffect {
    void apply(GameContext context);
}

