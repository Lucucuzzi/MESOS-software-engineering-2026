package it.polimi.ingsw.am46.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Board rules.
 */
public class BoardRules {

    private BoardRules() {}

    /**
     * Create turn tile spaces list.
     *
     * @param numPlayers the num players
     * @return the list
     */
    public static List<Space> createTurnTileSpaces(int numPlayers) {
        List<Space> spaces = new ArrayList<>();

        int[][] config = switch(numPlayers) {
            case 2 -> new int[][] {
                    {1, 1, 0},
                    {2, -1, -2}
            };
            case 3 -> new int[][] {
                    {1, 2, 0},
                    {2, 0, 0},
                    {3, -1, -2}
            };
            case 4 -> new int[][] {
                    {1, 2, 0},
                    {2, 1, 0},
                    {3, 0, 0},
                    {4, -1, -2}
            };
            case 5 -> new int[][] {
                    {1, 3, 0},
                    {2, 1, 0},
                    {3, 0, 0},
                    {4, 0, 0},
                    {5, -1, -2}
            };
            default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
        };

        // create the list of spaces ready to be used
        for (int[] spaceData : config) {
            spaces.add(new Space(spaceData[0], spaceData[1], spaceData[2]));
        }
        return spaces;
    }

    /**
     * Create offer tiles list.
     *
     * @param numPlayers the num players
     * @return the list
     */
    public static List<OfferTile> createOfferTiles(int numPlayers) {
        List<OfferTile> tiles = new ArrayList<>();

        // Local record for type-safety (no more weird casts from int to char)
        record TileConfig(char id, int number, int cardsFromDown, int cardsFromAbove, int food) {}

        TileConfig[] tileConfigs = switch(numPlayers) {
            case 2 -> new TileConfig[] {
                    new TileConfig('B', 2, 0, 1, 0),
                    new TileConfig('C', 2, 1, 0, 0),
                    new TileConfig('E', 2, 1, 1, 0),
                    new TileConfig('F', 2, 2, 0, 0)
            };
            case 3 -> new TileConfig[] {
                    new TileConfig('B', 2, 0, 1, 0),
                    new TileConfig('C', 2, 1, 0, 0),
                    new TileConfig('D', 3, 0, 2, 0),
                    new TileConfig('E', 2, 1, 1, 0),
                    new TileConfig('F', 2, 2, 0, 0)
            };
            case 4 -> new TileConfig[] {
                    new TileConfig('B', 2, 0, 1, 0),
                    new TileConfig('C', 2, 1, 0, 0),
                    new TileConfig('D', 3, 0, 2, 0),
                    new TileConfig('E', 2, 1, 1, 0),
                    new TileConfig('F', 2, 2, 0, 0),
                    new TileConfig('G', 4, 2, 1, 0)
            };
            case 5 -> new TileConfig[] {
                    new TileConfig('A', 5, 0, 0, 3),
                    new TileConfig('B', 2, 0, 1, 0),
                    new TileConfig('C', 2, 1, 0, 0),
                    new TileConfig('D', 3, 0, 2, 0),
                    new TileConfig('E', 2, 1, 1, 0),
                    new TileConfig('F', 2, 2, 0, 0),
                    new TileConfig('G', 4, 2, 1, 0)
            };
            default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
        };

        // create the list of offerTiles to be used
        for (TileConfig config : tileConfigs) {
            tiles.add(new OfferTile(
                    config.id(),
                    config.number(),
                    config.cardsFromDown(),
                    config.cardsFromAbove(),
                    config.food()
            ));
        }

        return tiles;
    }

    /**
     * Gets buildings per era.
     *
     * @param numPlayers the num players
     * @param era        the era
     * @return the buildings per era
     */
    public static int getBuildingsPerEra(int numPlayers, int era) {
        return switch(era) {
            case 1 -> switch(numPlayers) {
                case 2 -> 1;
                case 3 -> 2;
                case 4 -> 2;
                case 5 -> 2;
                default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
            };
            case 2 -> switch(numPlayers) {
                case 2 -> 2;
                case 3 -> 2;
                case 4 -> 3;
                case 5 -> 3;
                default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
            };
            case 3 -> switch(numPlayers) {
                case 2 -> 3;
                case 3 -> 4;
                case 4 -> 4;
                case 5 -> 5;
                default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
            };
            default -> throw new IllegalArgumentException("Unsupported era: " + era);
        };
    }

}
