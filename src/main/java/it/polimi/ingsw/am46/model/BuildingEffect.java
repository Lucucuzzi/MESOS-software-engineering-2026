package it.polimi.ingsw.am46.model;

@FunctionalInterface
public interface BuildingEffect {
    void apply(GameContext context);
}
