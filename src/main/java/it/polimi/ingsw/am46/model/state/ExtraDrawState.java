package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;

public class ExtraDrawState extends RoundPhase{

    public ExtraDrawState(){
        super(TriggerType.ONEXTRADRAW);
    }

    @Override
    public void handleDrawExtraCard(GameContext ctx, Player player, Card extraCard) {
    }

    @Override
    public void nextPhase(GameContext ctx) {
    }
}
