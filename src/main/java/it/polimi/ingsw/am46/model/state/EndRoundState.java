package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.TriggerType;

public class EndRoundState extends RoundPhase{

    public EndRoundState(){
        super(TriggerType.ENDTURN);
    }
}
