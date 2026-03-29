package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;

public class EndRoundState extends RoundPhase{

    public EndRoundState(){
        super(TriggerType.ENDTURN);
    }

    @Override
    public boolean isFinalPhase() {
        return true;  // This is the final phase of each round
    }

    @Override
    public void handleEndRound(GameContext ctx) {


        // === CHECK IF GAME IS OVER ===
        if (ctx.isGameOver()) {
                // Calculate final prestige points for all players
                ctx.countFinalPoints();

                // Winner is now determined via ctx.getWinner()
                Player winner = ctx.getWinner();

                // Game state machine ends here, the controller
                //will handle the rest of the operations, like showing
                //in the display the winner

            }
        else {
            // Game continues to next round
            nextPhase(ctx);
        }
    }

    @Override
    public void nextPhase(GameContext ctx) {
        // This is called only if game isn't over,transition to the first phase of the next round
        RoundPhase next = new PlaceTotemState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
}
