package it.polimi.ingsw.am46.model;

import java.util.ArrayList;
import java.util.List;

public class BoardRules {

    // turnTile configuration
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

    // offertTiles configuration
    public static List<OfferTile> createOfferTiles(int numPlayers) {
        List<OfferTile> tiles = new ArrayList<>();
        char[] tileIds = {'A', 'B', 'C', 'D', 'E', 'F'};

        int[][] tileConfigs = switch(numPlayers) {
            case 2 -> new int[][] {
                    {'B', 2, 0, 1, 0},
                    {'C', 2, 1, 0, 0},
                    {'E', 2, 1, 1, 0},
                    {'F', 2, 2, 0, 0}
            };
            case 3 -> new int[][] {
                    {'B', 2, 0, 1, 0},
                    {'C', 2, 1, 0, 0},
                    {'D', 3, 0, 2, 0},
                    {'E', 2, 1, 1, 0},
                    {'F', 2, 2, 0, 0}
            };
            case 4 -> new int[][] {
                    {'B', 2, 0, 1, 0},
                    {'C', 2, 1, 0, 0},
                    {'D', 3, 0, 2, 0},
                    {'E', 2, 1, 1, 0},
                    {'F', 2, 2, 0, 0},
                    {'G', 4, 2, 1, 0}
            };
            case 5 -> new int[][] {
                    {'A', 5, 0, 0, 3},
                    {'B', 2, 0, 1, 0},
                    {'C', 2, 1, 0, 0},
                    {'D', 3, 0, 2, 0},
                    {'E', 2, 1, 1, 0},
                    {'F', 2, 2, 0, 0},
                    {'G', 4, 2, 1, 0}
            };

            default -> throw new IllegalArgumentException("Unsupported number of players: " + numPlayers);
        };

        // create the list of offerTiles to be used
        for (int[] config : tileConfigs) {
            tiles.add(new OfferTile(
                    (char)config[0], // (char)letter
                    config[1], // number
                    config[2], // numCardFromDown
                    config[3], // numCardFromAbove
                    config[4]  // food
            ));
        }
        return tiles;
    }

    // building configuration
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
