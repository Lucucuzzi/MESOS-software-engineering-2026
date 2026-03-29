package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.Type;

import java.util.ArrayList;

public class ExtraDrawState extends RoundPhase{

    private ArrayList<Player> eligiblePlayers;

    public ExtraDrawState(){
        super(TriggerType.ONEXTRADRAW);
        eligiblePlayers = new ArrayList<>();
    }

    public void startPhase(GameContext ctx) {
        // Check if at least one player can take an extra card
        for (Player p : ctx.getPlayers()) {
            if (p.canTakeExtraCard()) {
                eligiblePlayers.add(p);
            }
        }
        // If no extra draw is available, skip this phase entirely
        if (eligiblePlayers.isEmpty()) {
            nextPhase(ctx);
        } else {
            ctx.setActivePlayer(eligiblePlayers.getFirst());
        }

    }

    @Override
    public void handleDrawExtraCard(GameContext ctx, Player player, Card extraCard) {
        //control if player can take an extra card
        if (!eligiblePlayers.contains(player)) {
            throw new IllegalStateException("You cannot take an extra card right now!");
        }

        // CORREZIONE 2: permettiamo extraCard == null, perché l'effetto è facoltativo ("potete prendere").
        // Se un giocatore non vuole sprecare cibo, deve poter dire "non pesco nulla".
        if (extraCard != null) {

            // CORREZIONE 3: La carta extra deve essere presa SOLO dalla fila superiore (regolamento Mesos Edificio 11)
            boolean fromTop = ctx.getBoard().getTopRow().contains(extraCard);
            if (!fromTop) {
                throw new IllegalStateException("You can only draw extra cards from the TOP row!");
            }

            //control that the card isn't an event
            if(checkIfEvent(extraCard)) throw new IllegalStateException("You cannot add an Event Card!");

            //control if the player has enough food to pay for the card (considering builder discounts)
            if (!checkIfEnoughFood(player, extraCard)) {
                throw new IllegalStateException("Not enough food!");
            }

            //player pay the cost of the card
            modifyFood(player, extraCard);

            // Remove it from the board
            ctx.getBoard().removeFromBoard(extraCard);

            // to check how many pairs and sets player has before adding new card
            int olderInventorPairs = player.countInventorPairs();
            int olderCompleteSets = player.countCompleteSets();

            // player add the card
            addCardToPlayer(player, extraCard);

            // to check how many pairs and sets player has after adding new card
            int currentInventorPairs = player.countInventorPairs();
            int currentCompleteSets = player.countCompleteSets();

            player.setNewlyFormedInventorPairs(Math.max(0, currentInventorPairs - olderInventorPairs));
            player.setNewlyFormedSets(Math.max(0, currentCompleteSets - olderCompleteSets));

            // Trigger Building Effects
            triggerBuildingEffects(ctx, player, TriggerType.ADDCARD);
        }

        // Il giocatore ha completato la sua fase extra, lo togliamo dalla coda
        eligiblePlayers.remove(player);

        // CORREZIONE 4: Passa al prossimo giocatore eligibile o vai alla prossima fase
        if(eligiblePlayers.isEmpty()) {
            nextPhase(ctx);
        } else {
            ctx.setActivePlayer(eligiblePlayers.getFirst());
        }

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

    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new ResolveEventState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
}
