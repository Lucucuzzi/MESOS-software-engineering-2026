package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.TriggerType;

public class ResolveEventState extends RoundPhase{
    public ResolveEventState(){
        super(TriggerType.ONEVENT);
    }

    @Override
    public void handleResolveEvent(GameContext ctx) {
    }

    @Override
    public void nextPhase(GameContext ctx) {
    }
}
