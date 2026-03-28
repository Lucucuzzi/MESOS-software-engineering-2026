package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;

public class PlaceTotemState extends RoundPhase{
    public PlaceTotemState(){
        super(TriggerType.ONTOTEMPLACEMENT);
    }

    @Override
    public void handlePlaceTotem(GameContext ctx, Player player, OfferTile offerTile) {
    }

    @Override
    public void nextPhase(GameContext ctx) {
    }
}
