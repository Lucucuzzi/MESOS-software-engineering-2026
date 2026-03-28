package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.Type;

import java.util.ArrayList;

public class AddCardState extends RoundPhase{

    private int offerTileIndex;
    private int remainingTopDraws;
    private int remainingBottomDraws;

    public AddCardState() {
        super(TriggerType.ADDCARD);
    }

    @Override
    public void startPhase(GameContext ctx) {
        this.offerTileIndex = 0;
        findNextPlayerOnOfferTrack(ctx);
    }

    @Override
    public void advanceTurn(GameContext ctx) {
        this.offerTileIndex++;
        findNextPlayerOnOfferTrack(ctx);
    }

    private void findNextPlayerOnOfferTrack(GameContext ctx) {
        ArrayList<OfferTile> tiles = ctx.getBoard().getOfferTiles();

        while (offerTileIndex < tiles.size()) {
            OfferTile currentTile = tiles.get(offerTileIndex);
            Player totemOwner = currentTile.getTotem();

            if (totemOwner != null) {
                ctx.setActivePlayer(totemOwner);
                this.remainingTopDraws = currentTile.getNumCardFromAbove();
                this.remainingBottomDraws = currentTile.getNumCardFromDown();
                return;
            }
            offerTileIndex++;
        }

        // No more totems found, the phase is over
        nextPhase(ctx);
    }
    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new ResolveEventState();
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

        // Check and Validate Draw Row limits

        boolean isTopRow = isCardFromTopRow(ctx, card);
        validateDrawAvailability(isTopRow);

        // validate food

        if (!checkIfEnoughFood(player, card)) {
            throw new IllegalStateException("Not enough food!");
        }
        //player pay the cost of the card
        modifyFood(player, card);

        // to check how many pairs and sets player has before adding new card
        int olderInventorPairs = player.countInventorPairs();
        int olderCompleteSets = player.countCompleteSets();

        // player add the card
        addCardToPlayer(player, card);

        // Remove it from the board
        ctx.getBoard().removeFromBoard(card);

        // to check how many pairs and sets player has after adding new card
        int currentInventorPairs = player.countInventorPairs();
        int currentCompleteSets = player.countCompleteSets();

        player.setNewlyFormedInventorPairs(Math.max(0, currentInventorPairs - olderInventorPairs));
        player.setNewlyFormedSets(Math.max(0, currentCompleteSets - olderCompleteSets));

        // Trigger Building Effects
        triggerBuildingEffects(ctx, player, TriggerType.ADDCARD);

        // Update draw counters and end turn if done
        updateCounters(isTopRow);
        if (remainingTopDraws == 0 && remainingBottomDraws == 0) {
            advanceTurn(ctx);
        }

    }

    public boolean checkIfActivePlayer(GameContext ctx, Player player){
        return player == ctx.getActivePlayer(); // return false if player is not the active
    }


    public boolean checkIfEnoughFood(Player player, Card card){
        int newCost = card.getCost() - applyBuilderDiscount(player, card);
        return player.getFood() >= Math.max(0,newCost); //return false if food isn't enough
        // using math max because with builder discount for building, cost cannot go below zero
        // for characters, cost is zero by default (so it's unnecessary check if card is a building to apply discount)
    }
    public int applyBuilderDiscount(Player player, Card card){
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

}
