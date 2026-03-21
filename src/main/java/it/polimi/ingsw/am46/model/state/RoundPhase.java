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

    public void handlePlaceTotem(Player player, OfferTile offerTile){};

    public void handleAddCard(Player player, Card card){};
    public void handleResolveEvent(){};
    public void handleEndRound(){};
    public void nextPhase (){};
    public void ApplyBuildingEffect(RoundPhase roundPhase, GameContext ctx){};
}
