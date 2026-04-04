package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.Type;

public class BuildingCard extends Card {
    private final int pp;
    private final TriggerType triggerType;
    private final BuildingEffect strategy;

    public BuildingCard(int id, int era, int cost, int pp,  TriggerType triggerType, BuildingEffect strategy) {
        super(id, era, cost, Type.BUILDING);
        this.pp = pp;
        this.triggerType=triggerType;
        this.strategy=strategy;
    }
    public int getPp() {
        return pp;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public Type getType() {
        return Type.BUILDING;
    }

    @Override
    public void addToPlayer(Player player) {
        player.addCard(this);
    }

    public void applyEffect (GameContext cxt){
        this.strategy.apply(cxt);
    }

}
