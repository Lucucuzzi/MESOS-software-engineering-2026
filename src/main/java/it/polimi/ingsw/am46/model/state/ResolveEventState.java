package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.TriggerType;

public class ResolveEventState extends RoundPhase{
    public ResolveEventState(){
        super(TriggerType.ONEVENT);
    }
    @Override
    public void startPhase(GameContext ctx) {
        handleResolveEvent(ctx);
    }

    @Override
    public void handleResolveEvent(GameContext ctx) {
        if (ctx.getRound() == 10 && ctx.getCurrentEra() == 3) {
            // Esegue resolveAllEvents() implementato in Game.java
            ctx.resolveAllEvents();
        } else {
            // Esegue la normale resolveEvents() per i round da 1 a 9
            ctx.resolveEvents();
        }
        nextPhase(ctx);

    }

    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new EndRoundState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
}
