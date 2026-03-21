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

        // Map to store the total icon count for each player to avoid recalculation
        Map<Player, Integer> playerTotalIcons = new HashMap<>();

        // 1. Initial calculation: find maximums, minimums, and save the totals
        for (Player player : gameContext.getPlayers()) {
            int baseIcons = player.getCharacters().stream()
                    .filter(c -> c.getSubType() == SubType.SHAMAN)
                    .mapToInt(c -> ((Shaman) c).getStars())
                    .sum();

            int totalIcons = baseIcons + player.getTemporaryShamanIcons();
            playerTotalIcons.put(player, totalIcons);

            if (totalIcons > maxIcons) maxIcons = totalIcons;
            if (totalIcons < minIcons) minIcons = totalIcons;
        }

        // 2. Tie handling and winner counting
        int winnersCount = 0;
        for (int icons : playerTotalIcons.values()) {
            if (icons == maxIcons) {
                winnersCount++;
            }
        }

        // 3. PP assignment and state cleanup
        for (Player player : gameContext.getPlayers()) {
            int totalIcons = playerTotalIcons.get(player);

            // Majority Reward
            if (totalIcons == maxIcons) {
                int reward = this.winPP;
                // Double the reward only if the player is the SOLE winner
                if (player.getShamanDoublePP() && winnersCount == 1) {
                    reward *= 2;
                }
                player.modifyPP(reward);
            }
            // Minority Penalty
            if (totalIcons == minIcons) {
                // Apply penalty if the player does not have Shaman immunity
                if (!player.getShamanImmunity()) {
                    // The minus sign is explicit here. The JSON losePP value should be positive.
                    player.modifyPP(-this.losePP);
                }
            }
            // Reset temporary building effects for the next rounds
            player.setTemporaryShamanIcons(0);
            player.setShamanImmunity(false);
            player.setShamanDoublePP(false);
        }
    }

    @Override
    public SubType getSubType() {
        return SubType.SHR;
    }
}
