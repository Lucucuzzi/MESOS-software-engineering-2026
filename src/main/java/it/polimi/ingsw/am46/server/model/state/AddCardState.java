package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.server.model.*;
import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;

import java.util.ArrayList;

/**
 * The type Add card state.
 */
public class AddCardState extends RoundPhase{

    private int remainingTopDraws;
    private int remainingBottomDraws;
    private final ArrayList<Player> drawOrder;

    /**
     * Instantiates a new Add card state.
     */
    public AddCardState() {
        super(TriggerType.ADDCARD);
        this.drawOrder = new ArrayList<>();
    }

    @Override
    public void startPhase(GameContext ctx) {
        for (OfferTile tile : ctx.getBoard().getOfferTiles()) {
            if (tile.getTotem() != null) {
                drawOrder.add(tile.getTotem());
            }
        }
        advanceTurn(ctx);
    }

    @Override
    public void advanceTurn(GameContext ctx) {
        // If the queue is empty, all players have drafted their cards, the phase is over
        if (drawOrder.isEmpty()) {
            nextPhase(ctx);
            return;
        }

        Player nextActive = drawOrder.removeFirst();
        ctx.setActivePlayer(nextActive);

        // If the next player in drawOrder is already disconnected, skip them immediately.
        // This handles the case where a player disconnected during PlaceTotemState but
        // still got a totem placed (by handleSkipTurn) — they are in drawOrder but offline.
        if (nextActive.isDisconnected()) {
            moveTotemToTurnTile(ctx, nextActive);
            advanceTurn(ctx);
            return;
        }

        OfferTile currentTile = getOfferTileOfPlayer(ctx, nextActive);

        if (currentTile != null) {
            this.remainingTopDraws = currentTile.getNumCardFromAbove();
            this.remainingBottomDraws = currentTile.getNumCardFromDown();
        }


        if (ctx.getBoard().getTopRow().isEmpty()) {
            this.remainingTopDraws = 0;
        }

        long bottomAvailable = ctx.getBoard().getBottomRow().stream()
                .filter(c -> c.getType() != Type.EVENT)
                .count();

        if (bottomAvailable == 0) {
            this.remainingBottomDraws = 0;
        }

        //if the player cannot afford any available cards, automatic skip
        if (remainingTopDraws > 0 && !canAffordAnyCard(nextActive, ctx.getBoard().getTopRow())) {
            remainingTopDraws = 0;
        }
        if (remainingBottomDraws > 0 && !canAffordAnyCard(nextActive,
                ctx.getBoard().getBottomRow().stream()
                        .filter(c -> c.getType() != Type.EVENT)
                        .collect(java.util.stream.Collectors.toList()))) {
            remainingBottomDraws = 0;
        }

        if (this.remainingTopDraws == 0 && this.remainingBottomDraws == 0) {
            moveTotemToTurnTile(ctx, nextActive);
            advanceTurn(ctx); // Automatically pass to the next player
        }
    }


    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new ExtraDrawState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }

    @Override
    public void handleAddCard(GameContext ctx, Player player, Card card){

        // validations
        if(!checkIfActivePlayer(ctx,player)){
            throw new IllegalStateException("It's not your turn!");
        }
        if(checkIfEvent(card)) throw new IllegalStateException("You cannot add an Event Card!");

        boolean isTopRow = isCardFromTopRow(ctx, card);
        validateDrawAvailability(isTopRow);

        if (!checkIfEnoughFood(player, card)) {
            throw new IllegalStateException("Not enough food!");
        }
        modifyFood(player, card);

        // to check how many pairs and sets player has before adding new card
        int olderInventorPairs = player.countInventorPairs();
        int olderCompleteSets = player.countCompleteSets();

        addCardToPlayer(player, card);

        ctx.getBoard().removeFromBoard(card);

        // to check how many pairs and sets player has after adding new card
        int currentInventorPairs = player.countInventorPairs();
        int currentCompleteSets = player.countCompleteSets();

        player.setNewlyFormedInventorPairs(Math.max(0, currentInventorPairs - olderInventorPairs));
        player.setNewlyFormedSets(Math.max(0, currentCompleteSets - olderCompleteSets));

        triggerBuildingEffects(ctx, player, TriggerType.ADDCARD);

        updateCounters(isTopRow);


        if (remainingTopDraws == 0 && remainingBottomDraws == 0) {
            moveTotemToTurnTile(ctx, player);
            advanceTurn(ctx);
            return;
        }

        //even if he still has draws left, if he can't afford any cards left, move on
        if (remainingTopDraws > 0 && !canAffordAnyCard(player, ctx.getBoard().getTopRow())) {
            remainingTopDraws = 0;
        }
        if (remainingBottomDraws > 0 && !canAffordAnyCard(player,
                ctx.getBoard().getBottomRow().stream()
                        .filter(c -> c.getType() != Type.EVENT)
                        .collect(java.util.stream.Collectors.toList()))) {
            remainingBottomDraws = 0;
        }

        if (remainingTopDraws == 0 && remainingBottomDraws == 0) {
            moveTotemToTurnTile(ctx, player);
            advanceTurn(ctx);
        }

    }

    /**
     * Skips the remaining card draws for a disconnected player.
     *
     * Called by Game.skipPlayerTurn() → ServerController.advancePastDisconnectedPlayer().
     *
     * Two cases:
     * 1. The disconnected player is the CURRENT active player (they dropped mid-turn):
     *    zero out their remaining draws, move their totem to TurnTile, advance.
     * 2. The disconnected player is somewhere LATER in drawOrder (they dropped but their
     *    turn hasn't come yet): remove them from the queue. advanceTurn() will naturally
     *    skip them when their slot arrives (see the isDisconnected check in advanceTurn).
     *
     * Note: we do NOT attempt to auto-pick cards for the disconnected player.
     * Taking a card has food cost implications and building triggers. Auto-picking
     * would silently modify the player's state in ways they cannot consent to.
     * The correct behavior is to simply forfeit their draws for this round.
     */
    @Override
    public void handleSkipTurn(GameContext ctx, Player player) {
        // Check if the disconnected player is the current active user
        if (ctx.getActivePlayer() != null && ctx.getActivePlayer().equals(player)) {
            System.out.println("[Resilience] Active player disconnected while drafting cards.");
            // Let's reset his current fishing counters
            remainingTopDraws = 0;
            remainingBottomDraws = 0;
            drawOrder.remove(player);

            // Let's move the totem and move on to the next player online
            moveTotemToTurnTile(ctx, player);
            advanceTurn(ctx);
        } else {
            System.out.println("[Resilience] Queued player disconnected. Silent removal from track.");
            // Let's simply remove the player from the queue for this round
            drawOrder.remove(player);

            // We clean the board by first moving its totem onto the TurnTile
            moveTotemToTurnTile(ctx, player);
            // DO NOT call advanceTurn(ctx) so as not to disturb the active player!
        }
    }
    private void moveTotemToTurnTile(GameContext ctx, Player player) {
        OfferTile currentTile = getOfferTileOfPlayer(ctx, player);

        if (currentTile != null) {
            currentTile.removeTotem();
        }


        TurnTile turnTile = ctx.getBoard().getTurnTile();
        if (turnTile != null) {
            // Push the totem to the first available spot on the TurnTile
            turnTile.pushTotem(player);

            // Find the space the player just landed on
            Space landedSpace = turnTile.getSpaceOfPlayer(player);
            if (landedSpace != null) {
                // Apply standard space effects
                turnTile.applyTTEffect(landedSpace);

                // Trigger Building EFFECT10 (Gain +1 extra food if the space gives food)
                triggerBuildingEffects(ctx, player, TriggerType.ONTOTEMREPLACEMENT);
            }
        }
    }


    /**
     * Check if active player boolean.
     *
     * @param ctx    the ctx
     * @param player the player
     * @return the boolean
     */
    public boolean checkIfActivePlayer(GameContext ctx, Player player){
        return player == ctx.getActivePlayer();
    }


    /**
     * Check if enough food boolean.
     *
     * @param player the player
     * @param card   the card
     * @return the boolean
     */
    public boolean checkIfEnoughFood(Player player, Card card){
        int newCost = card.getCost() - applyBuilderDiscount(player, card);
        return player.getFood() >= Math.max(0,newCost);
        // using math max because with builder discount for building, cost cannot go below zero
    }

    /**
     * Apply builder discount int.
     *
     * @param player the player
     * @param card   the card
     * @return the int
     */
    public int applyBuilderDiscount(Player player, Card card){
        if (card.getType() != Type.BUILDING) {
            return 0;
        }
        int builderDiscount = 0;
        for (CharacterCard c : player.getCharacters()){
            builderDiscount += c.getDiscount();
        }
        return builderDiscount;
    }

    /**
     * Check if event boolean.
     *
     * @param card the card
     * @return the boolean
     */
    public boolean checkIfEvent(Card card){
        return card.getType() == Type.EVENT;
    }

    /**
     * Modify food.
     *
     * @param player the player
     * @param card   the card
     */
    public void modifyFood(Player player, Card card){
        int cost =  card.getCost() - applyBuilderDiscount(player, card);
        int payed = Math.max(0, cost);
        player.modifyFood(-payed);
    }

    private void addCardToPlayer(Player player, Card card) {
        card.addToPlayer(player);
    }

    private boolean isCardFromTopRow(GameContext ctx, Card card) {
        if (ctx.getBoard().getTopRow().contains(card)) return true;
        if (ctx.getBoard().getBottomRow().contains(card)) return false;
        throw new IllegalArgumentException("The selected card is not on the board.");
    }

    private void validateDrawAvailability(boolean isTopRow) {
        if (isTopRow && remainingTopDraws <= 0) {
            throw new IllegalStateException("You cannot draw any more cards from the top row.");
        }
        if (!isTopRow && remainingBottomDraws <= 0) {
            throw new IllegalStateException("You cannot draw any more cards from the bottom row.");
        }
    }
    private void updateCounters(boolean isTopRow) {
        if (isTopRow) remainingTopDraws--;
        else remainingBottomDraws--;
    }

    /**
     * Check if the player can afford at least one card from the list.
     * If no card is accessible, the turn is automatically skipped.
     */
    private boolean canAffordAnyCard(Player player, java.util.List<Card> cards) {
        for (Card card : cards) {
            int cost = card.getCost() - applyBuilderDiscount(player, card);
            if (player.getFood() >= Math.max(0, cost)) {
                return true;
            }
        }
        return false;
    }

    private OfferTile getOfferTileOfPlayer(GameContext ctx, Player player) {
        for (OfferTile tile : ctx.getBoard().getOfferTiles()) {
            if (tile.getTotem() == player) {
                return tile;
            }
        }
        return null;
    }

}