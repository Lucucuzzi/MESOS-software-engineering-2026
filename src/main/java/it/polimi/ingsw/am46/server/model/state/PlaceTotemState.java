package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.server.model.*;

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
        // Rimuovi subito i disconnessi — non partecipano a questo round
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
        // Salta i disconnessi prima di impostare il prossimo active player
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

        // Avanza solo se era l'active player
        if (ctx.getActivePlayer() != null && ctx.getActivePlayer().equals(player)) {
            placementOrder.removeIf(Player::isDisconnected);
            if (!placementOrder.isEmpty()) {
                ctx.setActivePlayer(placementOrder.getFirst());
            } else {
                nextPhase(ctx);
            }
        }
        // Se era in coda: la rimozione da placementOrder è sufficiente.
        // startPhase del round successivo farà removeIf di nuovo.
    }

    /**
     * Advances the placement order to the next connected (non-disconnected) player.
     * If no connected players remain in the queue, transitions to the next phase.
     *
     * This method is called ONLY when the disconnected player was the active player,
     * so it's safe to change ctx.setActivePlayer() here.
     *
     * @param ctx the game context to update
     */
    /*private void advanceToNextConnected(GameContext ctx) {
        System.out.println("[Resilience] advanceToNextConnected: placementOrder prima = " +
                placementOrder.stream().map(Player::getNickname).toList());

        // Rimuovi TUTTI i giocatori disconnessi dalla coda (non solo il primo)
        // Questo gestisce il caso di multiple disconnessioni ravvicinate
        placementOrder.removeIf(Player::isDisconnected);

        System.out.println("[Resilience] placementOrder dopo removeIf = " +
                placementOrder.stream().map(Player::getNickname).toList());

        if (placementOrder.isEmpty()) {
            // Nessun giocatore connesso rimasto in coda → fase completata
            System.out.println("[Resilience] placementOrder vuoto → nextPhase()");
            nextPhase(ctx);
        } else {
            // Imposta come active player il primo connesso rimasto
            Player nextActive = placementOrder.getFirst();
            ctx.setActivePlayer(nextActive);
            System.out.println("[Resilience] Nuovo active player: " + nextActive.getNickname());
        }
    } */



    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new AddCardState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);

    }

}
