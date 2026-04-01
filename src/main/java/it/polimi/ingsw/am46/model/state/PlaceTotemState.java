package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.*;

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
            ctx.setActivePlayer(placementOrder.getFirst());
        } else {
            nextPhase(ctx);
        }
    }

    @Override
    public void handlePlaceTotem(GameContext ctx, Player player, OfferTile offerTile) {
        if (offerTile.isOccupied()) {
            throw new IllegalStateException("This offer tile is already occupied!");
        }
        Space playerSpace = ctx.getBoard().getTurnTile().getSpaceOfPlayer(player);
        if (playerSpace != null) {
            playerSpace.setPlayer(null);
        }
        offerTile.placeTotem(player);
        //Add food to the player's reserve if the offer tile has some food
        player.modifyFood(offerTile.getFood());
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
