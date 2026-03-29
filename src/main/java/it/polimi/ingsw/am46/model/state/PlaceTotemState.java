package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;

import java.util.ArrayList;
import java.util.List;

public class PlaceTotemState extends RoundPhase{
    private List<Player> placementOrder;

    public PlaceTotemState(){
        super(TriggerType.ONTOTEMPLACEMENT);
        this.placementOrder = new ArrayList<>();
    }

    @Override
    public void startPhase(GameContext ctx) {
        List<Player> orderFromBoard = ctx.getBoard().getTurnTile().getTurnOrder();
        this.placementOrder = new ArrayList<>(orderFromBoard);
        if (!placementOrder.isEmpty()) {
            ctx.setActivePlayer(placementOrder.removeFirst());
        } else {
            // Se in qualche modo la lista fosse vuota, passa subito alla fase successiva
            nextPhase(ctx);
        }
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
        //Removes the player from the queue
        placementOrder.removeFirst();
        if (!placementOrder.isEmpty()) {
            ctx.setActivePlayer(placementOrder.getFirst());
        } else {
            nextPhase(ctx);
        }
    }




    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new AddCardState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);

    }
}
