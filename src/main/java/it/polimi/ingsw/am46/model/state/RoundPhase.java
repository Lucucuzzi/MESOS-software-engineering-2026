package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;

public abstract class RoundPhase {
    private TriggerType triggerType;

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void handlePlaceTotem(Player player, OfferTile offerTile){
        throw new IllegalStateException("You cannot place totem in this phase!");
    };

    public void handleAddCard(Player player, Card card){
        throw new IllegalStateException("You cannot add card in this phase!");
    };
    public void handleResolveEvent(){
        throw new IllegalStateException("SYSTEM ERROR : You cannot resolve this phase!");
    };
    public void handleDrawExtraCard(Player player, Card extraCard){
        throw new IllegalStateException("You cannot draw extra card in this phase!");
    }
    public void handleEndRound(){
        throw new IllegalStateException("SYSTEM ERROR : You cannot end round in this phase!");
    };

    public void nextPhase (){
    };

    public void ApplyBuildingEffect(RoundPhase roundPhase, GameContext ctx){
        throw new IllegalStateException("Building effects cannot be applied this way here!");
    };
}
