package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.server.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Place totem state.
 */
public class PlaceTotemState extends RoundPhase{
    private List<Player> placementOrder;

    /**
     * Instantiates a new Place totem state.
     */
    public PlaceTotemState(){
        super(TriggerType.ONTOTEMPLACEMENT);
        this.placementOrder = new ArrayList<>();
    }

    @Override
    public void startPhase(GameContext ctx) {
        List<Player> orderFromBoard = ctx.getBoard().getTurnTile().getTurnOrder();
        this.placementOrder = new ArrayList<>(orderFromBoard);
        // Remove disconnected people now — they are not participating in this round
        placementOrder.removeIf(Player::isDisconnected);

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
        // Skip disconnected before setting next active player
        placementOrder.removeIf(Player::isDisconnected);

        if (!placementOrder.isEmpty()) {
            ctx.setActivePlayer(placementOrder.getFirst());
        } else {
            nextPhase(ctx);
        }
    }

    /*
     * Forces a disconnected player's totem placement.
     * Strategy: place them on the LAST available OfferTile (worst position),
     * since they are not present to choose. This is consistent with the game rule
     * that later-placed totems get worse tiles, and it is fairer to connected players.
     * If ALL offer tiles are already occupied (edge case: everyone else placed before
     * the disconnected player got a chance, which can't normally happen in PlaceTotemState
     * since the order is strict), we simply remove them from the queue and advance.
     * After placing (or skipping if no tile available), the next player in
     * placementOrder who is still connected becomes active. If no connected player
     * remains in the queue, we advance to the next phase.
     */
    @Override
    public void handleSkipTurn(GameContext ctx, Player player) {
        placementOrder.remove(player);

        // Advance only if it was the active player
        if (ctx.getActivePlayer() != null && ctx.getActivePlayer().equals(player)) {
            placementOrder.removeIf(Player::isDisconnected);
            if (!placementOrder.isEmpty()) {
                ctx.setActivePlayer(placementOrder.getFirst());
            } else {
                nextPhase(ctx);
            }
        }
        // If it was queued: Removing from placementOrder is enough.
        // next round's startPhase will do removeIf again.
    }




    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new AddCardState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);

    }

}
