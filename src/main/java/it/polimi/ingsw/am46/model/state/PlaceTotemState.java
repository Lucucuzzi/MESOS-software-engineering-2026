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
        //Check if the offer tile is not already occupied
        if (offerTile.isOccupied()) {
            throw new IllegalStateException("This offer tile is already occupied!");
        }
        //Place the totem on the offer tile
        offerTile.placeTotem(player);
        //Add food to the player's reserve if the offer tile has some food
        player.modifyFood(offerTile.getFood());
        //Trigger building effects on totem placement
        triggerBuildingEffects(ctx, player, this.getTriggerType());
    }




    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new AddCardState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);

    }
}
