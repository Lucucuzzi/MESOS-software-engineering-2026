package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.TriggerType;

import java.util.List;

/**
 * The type End round state.
 */
public class EndRoundState extends RoundPhase{

    /**
     * Instantiates a new End round state.
     */
    public EndRoundState(){
        super(TriggerType.ENDTURN);
    }

    @Override
    public boolean isFinalPhase() {
        return true;  // This is the final phase of each round
    }

    public void startPhase(GameContext ctx){
        handleEndRound(ctx);
    }

    @Override
    public void handleEndRound(GameContext ctx) {

        //  CHECK IF GAME IS OVER
        if (ctx.isGameOver()) {
                ctx.countFinalPoints();
                // ADDED: trigger the PhaseChangeListener by calling setCurrentPhase
                // so ServerController sees isFinalPointsCounted = true and saves to the DB
                ctx.setCurrentPhase(this);
            }
        else {
            // Game continues to next round, resolveRound will clear the board
            ctx.resolveRound();
            nextPhase(ctx);
        }
    }

    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new PlaceTotemState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
    @Override
    public boolean isAutomatic() {
        return true;
    }
}
