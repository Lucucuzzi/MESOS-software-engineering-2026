package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;

public abstract class RoundPhase {
    private TriggerType triggerType;

    public RoundPhase(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void handlePlaceTotem(GameContext ctx, Player player, OfferTile offerTile){
        throw new IllegalStateException("You cannot place totem in this phase!");
    }

    public void handleAddCard(GameContext ctx,Player player, Card card){
        throw new IllegalStateException("You cannot add card in this phase!");
    }
    public void handleResolveEvent(GameContext ctx){
        throw new IllegalStateException("SYSTEM ERROR : You cannot resolve this phase!");
    }
    public void handleDrawExtraCard(GameContext ctx,Player player, Card extraCard){
        throw new IllegalStateException("You cannot draw extra card in this phase!");
    }
    public void handleEndRound(GameContext ctx){
        throw new IllegalStateException("SYSTEM ERROR : You cannot end round in this phase!");
    }

    public void nextPhase(){
    }

    // --- HELPER METHOD FOR BUILDINGS ---

    public void triggerBuildingEffects(GameContext ctx, Player player, TriggerType triggerType) {
        for (BuildingCard building : player.getBuildings()) {
            if (building.getTriggerType() == triggerType) {
                building.applyEffect(this, ctx);
            }
        }
    }
    // lifecycle methods
    public void startPhase(GameContext ctx) {}
    public void advanceTurn(GameContext ctx) {}
    public void nextPhase(GameContext ctx) {}
}
