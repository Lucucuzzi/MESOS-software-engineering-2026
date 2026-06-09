package it.polimi.ingsw.am46.server.model.state;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.OfferTile;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.TriggerType;
import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;

/**
 * The type Round phase.
 */
public abstract class RoundPhase {
    private TriggerType triggerType;

    /**
     * Instantiates a new Round phase.
     *
     * @param triggerType the trigger type
     */
    public RoundPhase(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * Gets trigger type.
     *
     * @return the trigger type
     */
    public TriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * Handle place totem.
     *
     * @param ctx       the ctx
     * @param player    the player
     * @param offerTile the offer tile
     */
    public void handlePlaceTotem(GameContext ctx, Player player, OfferTile offerTile){
        throw new IllegalStateException("You cannot place totem in this phase!");
    }

    /**
     * Handle add card.
     *
     * @param ctx    the ctx
     * @param player the player
     * @param card   the card
     */
    public void handleAddCard(GameContext ctx,Player player, Card card){
        throw new IllegalStateException("You cannot add card in this phase!");
    }

    /**
     * Handle resolve event.
     *
     * @param ctx the ctx
     */
    public void handleResolveEvent(GameContext ctx){
        throw new IllegalStateException("SYSTEM ERROR : You cannot resolve this phase!");
    }

    /**
     * Handle draw extra card.
     *
     * @param ctx       the ctx
     * @param player    the player
     * @param extraCard the extra card
     */
    public void handleDrawExtraCard(GameContext ctx,Player player, Card extraCard){
        throw new IllegalStateException("You cannot draw extra card in this phase!");
    }

    /**
     * Handle end round.
     *
     * @param ctx the ctx
     */
    public void handleEndRound(GameContext ctx){
        throw new IllegalStateException("SYSTEM ERROR : You cannot end round in this phase!");
    }

    /**
     * Handle skip turn.
     *
     * @param ctx    the ctx
     * @param player the player
     */
    public void handleSkipTurn(GameContext ctx, Player player) {
        // Default: no-op. Automatic phases don't need turn skipping.
    }


    // --- HELPER METHOD FOR BUILDINGS ---

    /**
     * Trigger building effects.
     *
     * @param ctx         the ctx
     * @param player      the player
     * @param triggerType the trigger type
     */
    public void triggerBuildingEffects(GameContext ctx, Player player, TriggerType triggerType) {
        for (BuildingCard building : player.getBuildings()) {
            if (building.getTriggerType() == triggerType) {
                building.applyEffect(ctx);
            }
        }
    }

    /**
     * Start phase.
     *
     * @param ctx the ctx
     */
    public void startPhase(GameContext ctx) {}

    /**
     * Advance turn.
     *
     * @param ctx the ctx
     */
    public void advanceTurn(GameContext ctx) {}

    /**
     * Next phase.
     *
     * @param ctx the ctx
     */
    public void nextPhase(GameContext ctx) {}

    /**
     * Is final phase boolean.
     *
     * @return the boolean
     */
    public boolean isFinalPhase() {
        return false;// Default implementation, can be overridden by specific phases
    }

    /**
     * Is automatic boolean.
     *
     * @return the boolean
     */
    public boolean isAutomatic(){
        return false;
    }
}
