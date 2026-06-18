package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.cli.utils.formatter.CLIFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Tile printer.
 */
public class TilePrinter {

    /**
     * The constant TILE_HEIGHT.
     */
    public static final int TILE_HEIGHT = 9;
    private static final int W = 20;

    /**
     * Print track.
     *
     * @param state the state
     */
    public static void printTrack(GameState state) {
        System.out.println(ColorCode.BOLD + "---  OFFER TRACK ---" + ColorCode.RESET);
        List<OfferTileState> tiles = state.getOfferTileStates();

        int tilesPerRow = 5;
        for (int i = 0; i < tiles.size(); i += tilesPerRow) {
            List<OfferTileState> chunk = tiles.subList(i, Math.min(i + tilesPerRow, tiles.size()));

            List<String[]> allTileLines = chunk.stream()
                    .map(tile -> getTileLines(tile, state))
                    .toList();

            for (int lineIdx = 0; lineIdx < TILE_HEIGHT; lineIdx++) {
                StringBuilder row = new StringBuilder();
                for (String[] tileLines : allTileLines) {
                    row.append(tileLines[lineIdx]).append("  ");
                }
                System.out.println(row);
            }
            System.out.println();
        }
    }

    /**
     * Get tile lines string [ ].
     *
     * @param tile  the tile
     * @param state the state
     * @return the string [ ]
     */
    public static String[] getTileLines(OfferTileState tile, GameState state) {
        String color = ColorCode.getTileColor();
        String R     = ColorCode.RESET;
        String B     = ColorCode.BOLD;

        String[] lines = new String[TILE_HEIGHT];

        lines[0] = color + " ." + "-".repeat(W) + "." + R;

        String title = "TILE [" + tile.getLetter() + "]";
        lines[1] = color + " | " + B + CLIFormatter.center(title, W - 2) + R + color + " |" + R;

        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        // CHANGED: "Free" to white if free, colored player name if occupied
        if (tile.isOccupied()) {
            PlayerState player = state.getPlayerStateByNickname(tile.getTotemOwnerNickname());
            String playerName  = ColorCode.playerName(player);
            int nameLen = tile.getTotemOwnerNickname().length();
            lines[3] = color + " | " + CLIFormatter.centerAnsi(playerName + R + color, nameLen, W - 2) + " |" + R;
        } else {
            String free = R + ColorCode.ITALIC + "Free" + R + color;
            lines[3] = color + " | " + CLIFormatter.centerAnsi(free, "Free".length(), W - 2) + " |" + R;
        }

        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // CHANGED: Top and Bottom on separate lines, Bottom written in full
        String[] attrs = getAttributes(tile);
        for (int i = 5; i < 8; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + CLIFormatter.fitLeft(text, W - 2) + " |" + R;
        }

        lines[8] = color + " '" + "-".repeat(W) + "'" + R;

        return lines;
    }

    // CHANGED: Top and Bottom on separate lines, "Bottom" written in full, Food unchanged
    private static String[] getAttributes(OfferTileState tile) {
        int top  = tile.getTopRow();
        int bot  = tile.getBottomRow();
        int food = tile.getFood();

        List<String> attrs = new ArrayList<>();
        if (top  != 0) attrs.add("Top: "    + top);
        if (bot  != 0) attrs.add("Bottom: " + bot);
        if (food != 0) attrs.add("Food: "   + (food > 0 ? "+" : "") + food);

        return attrs.toArray(new String[0]);
    }
}