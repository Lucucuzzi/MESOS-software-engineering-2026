package it.polimi.ingsw.am46.view.utils;

/**
 * Contains static descriptions for fixed board elements.
 */
public class BoardDictionary {

    /**
     * Returns the exact effect for a Turn Tile space based on position and player count.
     * @param position The 1-based position on the turn tile
     * @param numPlayers The total number of players in the game
     * @return A formatted string describing the bonus or penalty
     */
    public static String getTurnTileEffect(int position, int numPlayers) {
        int[] spaceData = getSpaceData(position, numPlayers);

        if (spaceData == null) return "Unknown position";

        int foodModifier = spaceData[1];
        int ppModifier = spaceData[2];

        if (foodModifier == 0 && ppModifier == 0) {
            return "No effect";
        }

        StringBuilder effect = new StringBuilder();
        if (foodModifier > 0) effect.append("+").append(foodModifier).append(" Food ");
        else if (foodModifier < 0) effect.append(foodModifier).append(" Food ");

        if (ppModifier > 0) effect.append("+").append(ppModifier).append(" PP");
        else if (ppModifier < 0) effect.append(ppModifier).append(" PP");

        return effect.toString().trim();
    }

    /**
     * Internal mapping mirroring the Game Design rules.
     */
    private static int[] getSpaceData(int position, int numPlayers) {
        return switch (numPlayers) {
            case 2 -> switch (position) {
                case 1 -> new int[]{1, 1, 0};
                case 2 -> new int[]{2, -1, -2};
                default -> null;
            };
            case 3 -> switch (position) {
                case 1 -> new int[]{1, 2, 0};
                case 2 -> new int[]{2, 0, 0};
                case 3 -> new int[]{3, -1, -2};
                default -> null;
            };
            case 4 -> switch (position) {
                case 1 -> new int[]{1, 2, 0};
                case 2 -> new int[]{2, 1, 0};
                case 3 -> new int[]{3, 0, 0};
                case 4 -> new int[]{4, -1, -2};
                default -> null;
            };
            case 5 -> switch (position) {
                case 1 -> new int[]{1, 3, 0};
                case 2 -> new int[]{2, 1, 0};
                case 3 -> new int[]{3, 0, 0};
                case 4 -> new int[]{4, 0, 0};
                case 5 -> new int[]{5, -1, -2};
                default -> null;
            };
            default -> null;
        };
    }
}