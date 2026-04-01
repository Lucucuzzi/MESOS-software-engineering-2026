package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.Type;

import java.util.ArrayList;

public class AddCardState extends RoundPhase{

    private int remainingTopDraws;
    private int remainingBottomDraws;
    private final ArrayList<Player> drawOrder;

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


    public boolean checkIfActivePlayer(GameContext ctx, Player player){
        return player == ctx.getActivePlayer();
    }


    public boolean checkIfEnoughFood(Player player, Card card){
        int newCost = card.getCost() - applyBuilderDiscount(player, card);
        return player.getFood() >= Math.max(0,newCost);
        // using math max because with builder discount for building, cost cannot go below zero
    }
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
    public boolean checkIfEvent(Card card){
        return card.getType() == Type.EVENT;
    }
    public void modifyFood(Player player, Card card){
        int cost =  card.getCost() - applyBuilderDiscount(player, card);
        int payed = Math.max(0, cost);
        player.modifyFood(-payed);
    }

    private void addCardToPlayer(Player player, Card card) {
        if (card.getType() == Type.BUILDING) {
            player.addCard((BuildingCard) card);
        } else if (card.getType() == Type.CHARACTER) {
            player.addCard((CharacterCard) card);
        }
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
    private OfferTile getOfferTileOfPlayer(GameContext ctx, Player player) {
        for (OfferTile tile : ctx.getBoard().getOfferTiles()) {
            if (tile.getTotem() == player) {
                return tile;
            }
        }
        return null;
    }

}
