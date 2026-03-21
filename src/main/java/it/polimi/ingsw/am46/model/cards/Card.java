package it.polimi.ingsw.am46.model.cards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;

public abstract class Card {
    private final int id, era, cost;
    private final Type type;

    public Card(int id, int era, int cost, Type type) {
        this.id = id;
        this.era = era;
        this.cost = cost;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public int getEra() {
        return era;
    }
    public int getCost() {
        return cost;
    }
    public Type getType() {
        return type;
    }

    public void applyEffect(Player player) {}
    public void resolve(GameContext gameContext) {}





}
