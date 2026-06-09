package it.polimi.ingsw.am46.server.model.cards.eventCards;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

/**
 * The type Sustenance.
 */
public class Sustenance extends EventCard {
    private final int ppPenalty;

    /**
     * Instantiates a new Sustenance.
     *
     * @param id         the id
     * @param era        the era
     * @param cost       the cost
     * @param finalEvent the final event
     * @param ppPenalty  the pp penalty
     */
    public Sustenance(int id, int era, int cost, boolean finalEvent, int ppPenalty) {
        super(id, era, cost, SubType.SUSTENANCE, finalEvent);
        this.ppPenalty = ppPenalty;
    }

    @Override
    public void resolve(GameContext gameContext) {
       for (Player player : gameContext.getPlayers()) {
           int totalMouths = player.getCharacters().size();
           int gathererDiscount = player.countCharactersByType(SubType.GATHERER) * 3;
           int buildingDiscount = player.getSustenanceDiscount();
           int finalCost = Math.max(0, totalMouths - gathererDiscount - buildingDiscount);
           int foodPaid = Math.min(player.getFood(),finalCost);
           player.modifyFood(-foodPaid);
           int unfed = finalCost -  foodPaid;
           if (unfed > 0) player.modifyPP(-(unfed * this.ppPenalty));
           player.resetSustenanceDiscount();
       }
    }

    @Override
    public SubType getSubType() {
        return SubType.SUSTENANCE;
    }
}
