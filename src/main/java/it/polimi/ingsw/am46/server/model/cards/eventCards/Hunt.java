package it.polimi.ingsw.am46.server.model.cards.eventCards;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

public class Hunt extends EventCard {
    private final int ppHunter;

    public Hunt(int id, int era, int cost, boolean finalEvent, int ppHunter) {
        super(id, era, cost, SubType.HUNT, finalEvent);
        this.ppHunter = ppHunter;
    }

    @Override
    public void resolve(GameContext gameContext) {
        for (Player player : gameContext.getPlayers()) {
            int hunters = player.countCharactersByType(SubType.HUNTER);
            if (hunters > 0) {
                player.modifyFood(hunters);
                player.modifyPP(this.ppHunter * hunters);
            }
        }
    }

    @Override
    public SubType getSubType() {
        return SubType.HUNT;
    }
}
