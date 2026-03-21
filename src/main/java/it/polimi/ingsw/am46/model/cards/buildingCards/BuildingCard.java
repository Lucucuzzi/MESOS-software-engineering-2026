package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.BuildingEffect;
import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.RoundPhase;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public abstract class BuildingCard extends Card {
    private final int pp, food;
    private final TriggerType triggerType;
    private final BuildingEffect strategy;

    public BuildingCard(int id, int era, int cost, int pp, int food, TriggerType triggerType, BuildingEffect strategy) {
        super(id, era, cost, Type.BUILDING);
        this.pp = pp;
        this.food = food;
        this.triggerType=triggerType;
        this.strategy=strategy;
    }
    public int getPp() {
        return pp;
    }
    public int getFood() {
        return food;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public Type getType() {
        return Type.BUILDING;
    }

    public void applyEffect (RoundPhase currentPhase, GameContext cxt){
        if(currentPhase.getTriggerType()==this.getTriggerType())
            this.strategy.apply(cxt);
    }
}
