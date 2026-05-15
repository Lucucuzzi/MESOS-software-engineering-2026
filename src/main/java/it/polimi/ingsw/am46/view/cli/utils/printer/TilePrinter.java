package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;

import java.util.ArrayList;
import java.util.List;

public class TilePrinter {

    public static final int TILE_HEIGHT = 9;
    private static final int W = 20;

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

    public static String[] getTileLines(OfferTileState tile, GameState state) {
        String color = ColorCode.getTileColor();
        String R     = ColorCode.RESET;
        String B     = ColorCode.BOLD;

        String[] lines = new String[TILE_HEIGHT];

        lines[0] = color + " ." + "-".repeat(W) + "." + R;

        String title = "TILE [" + tile.getLetter() + "]";
        lines[1] = color + " | " + B + center(title, W - 2) + R + color + " |" + R;

        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        // CAMBIATO: "Free" in bianco se libera, nome giocatore colorato se occupata
        if (tile.isOccupied()) {
            PlayerState player = state.getPlayerStateByNickname(tile.getTotemOwnerNickname());
            String playerName  = ColorCode.playerName(player);
            int nameLen = tile.getTotemOwnerNickname().length();
            int pad = Math.max(0, (W - 2 - nameLen) / 2);
            lines[3] = color + " | " + " ".repeat(pad) + playerName + " ".repeat(W - 2 - nameLen - pad) + " |" + R;
        } else {
            // CAMBIATO: il reset va prima del bordo, così il '|' finale resta del colore della tile
            String free = R + ColorCode.ITALIC + "Free" + R + color;
            int pad = (W - 2 - "Free".length()) / 2;
            lines[3] = color + " | " + " ".repeat(pad) + free + " ".repeat(W - 2 - "Free".length() - pad) + " |" + R;
        }

        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // CAMBIATO: Top e Bottom su righe separate, Bottom scritto per intero
        String[] attrs = getAttributes(tile);
        for (int i = 5; i < 8; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + fitLeft(text, W - 2) + " |" + R;
        }

        lines[8] = color + " '" + "-".repeat(W) + "'" + R;

        return lines;
    }

    // CAMBIATO: Top e Bottom su righe separate, "Bottom" scritto per intero, Food invariato
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

    private static String center(String text, int len) {
        if (text.length() >= len) return text.substring(0, len);
        int left = (len - text.length()) / 2;
        return " ".repeat(left) + text + " ".repeat(len - text.length() - left);
    }

    private static String fitLeft(String text, int len) {
        if (text == null) text = "";
        if (text.length() > len) return text.substring(0, len);
        return text + " ".repeat(len - text.length());
    }
}