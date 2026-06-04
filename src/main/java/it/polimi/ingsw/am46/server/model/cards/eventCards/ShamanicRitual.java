package it.polimi.ingsw.am46.server.model.cards.eventCards;

import it.polimi.ingsw.am46.server.model.GameContext;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

import java.util.HashMap;
import java.util.Map;

public class ShamanicRitual extends EventCard {
    private final int winPP;
    private final int losePP;
    public ShamanicRitual(int id, int era, int cost, boolean finalEvent, int winPP, int losePP) {
        super(id, era, cost, SubType.SHR, finalEvent);
        this.winPP = winPP;
        this.losePP = losePP;
    }

    @Override
    public void resolve(GameContext gameContext) {
        int maxIcons = -1;
        int minIcons = Integer.MAX_VALUE;

        Map<Player, Integer> playerTotalIcons = new HashMap<>();

        for (Player player : gameContext.getPlayers()) {
            int totalIcons = player.countShamanIcons();
            playerTotalIcons.put(player, totalIcons);

            if (totalIcons > maxIcons) maxIcons = totalIcons;
            if (totalIcons < minIcons) minIcons = totalIcons;
        }

        int winnersCount = 0;
        for (int icons : playerTotalIcons.values()) {
            if (icons == maxIcons) winnersCount++;
        }

        for (Player player : gameContext.getPlayers()) {
            int totalIcons = playerTotalIcons.get(player);

            if (totalIcons == maxIcons) {
                int reward = this.winPP;
                if (player.hasShamanDoublePP() && winnersCount == 1) {
                    reward *= 2;
                }
                player.modifyPP(reward);
            }
        }

        for (Player player : gameContext.getPlayers()) {
            int totalIcons = playerTotalIcons.get(player);

            if (totalIcons == minIcons) {
                if (!player.hasShamanImmunity()) {
                    player.modifyPP(-this.losePP);
                }
            }

            player.resetShamanFlags();
        }
    }

    @Override
    public SubType getSubType() {
        return SubType.SHR;
    }
}
