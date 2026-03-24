package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.characterCards.Shaman;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

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

        // Map to store the total icon count for each player to avoid recalculations
        Map<Player, Integer> playerTotalIcons = new HashMap<>();

        // 1. Initial calculation: find maximums and minimums
        for (Player player : gameContext.getPlayers()) {
            int totalIcons = player.countShamanIcons();
            playerTotalIcons.put(player, totalIcons);

            if (totalIcons > maxIcons) maxIcons = totalIcons;
            if (totalIcons < minIcons) minIcons = totalIcons;
        }

        // 2. Count how many players share the maximum (required for the Double PP effect)
        int winnersCount = 0;
        for (int icons : playerTotalIcons.values()) {
            if (icons == maxIcons) winnersCount++;
        }

        // PHASE 1: All winners gain PP (Rule: "Everyone first gains PP...")
        for (Player player : gameContext.getPlayers()) {
            int totalIcons = playerTotalIcons.get(player);

            if (totalIcons == maxIcons) {
                int reward = this.winPP;
                // Double the reward ONLY if the player is the SOLE winner (Building effect)
                if (player.hasShamanDoublePP() && winnersCount == 1) {
                    reward *= 2;
                }
                player.modifyPP(reward);
            }
        }

        // PHASE 2: All losers lose PP (Rule: "...and then lose PP")
        for (Player player : gameContext.getPlayers()) {
            int totalIcons = playerTotalIcons.get(player);

            if (totalIcons == minIcons) {
                // Immunity applies if the player actually has "fewer icons than the others"

                if (!player.hasShamanImmunity()) {
                    player.modifyPP(-this.losePP);
                }
            }

            // State cleanup at the end of the event
            player.resetShamanFlags();
        }
    }

    @Override
    public SubType getSubType() {
        return SubType.SHR;
    }
}
