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

        //permettiamo extraCard == null, perché l'effetto è facoltativo ("potete prendere").
        if (extraCard != null) {

            boolean fromTop = ctx.getBoard().getTopRow().contains(extraCard);
            if (!fromTop) {
                throw new IllegalStateException("You can only draw extra cards from the TOP row!");
            }

            if(checkIfEvent(extraCard)) throw new IllegalStateException("You cannot add an Event Card!");

            if (!checkIfEnoughFood(player, extraCard)) {
                throw new IllegalStateException("Not enough food!");
            }

            modifyFood(player, extraCard);
            ctx.getBoard().removeFromBoard(extraCard);

            // to check how many pairs and sets player has before adding new card
            int olderInventorPairs = player.countInventorPairs();
            int olderCompleteSets = player.countCompleteSets();

            addCardToPlayer(player, extraCard);

            int currentInventorPairs = player.countInventorPairs();
            int currentCompleteSets = player.countCompleteSets();

            player.setNewlyFormedInventorPairs(Math.max(0, currentInventorPairs - olderInventorPairs));
            player.setNewlyFormedSets(Math.max(0, currentCompleteSets - olderCompleteSets));

            triggerBuildingEffects(ctx, player, TriggerType.ADDCARD);
        }

        // Il giocatore ha completato la sua fase extra, lo togliamo dalla coda
        eligiblePlayers.remove(player);

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

    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new ResolveEventState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
}
