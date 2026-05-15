package it.polimi.ingsw.am46.view.utils;

/**
 * Contains static descriptions for fixed board elements.
 */
public class BoardDictionary {

    /**
     * Returns the exact effect for a Turn Tile space based on position and player count.
     * @param position The 1-based position on the turn tile
     * @param numPlayers The total number of players in the game
     * @return an int that is food or pp
     */
    public static int getTurnTileFood(int position, int numPlayers) {
        int[] data = getSpaceData(position, numPlayers);
        return data != null ? data[1] : 0;
    }

    public static int getTurnTilePP(int position, int numPlayers) {
        int[] data = getSpaceData(position, numPlayers);
        return data != null ? data[2] : 0;
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