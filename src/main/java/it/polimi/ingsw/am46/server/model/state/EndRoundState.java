package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.TriggerType;

import java.util.List;

public class EndRoundState extends RoundPhase{

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
                List<Player> winner = ctx.getWinner();
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
