package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.cli.utils.formatter.CLIFormatter;
import it.polimi.ingsw.am46.view.utils.BoardDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Turn order printer.
 */
public class TurnOrderPrinter {

    /**
     * The constant TILE_HEIGHT.
     */
    public static final int TILE_HEIGHT = 8;
    private static final int W = 17;

    /**
     * Print turn order.
     *
     * @param state the state
     */
    public static void printTurnOrder(GameState state) {
        System.out.println(ColorCode.BOLD + "---  TURN TILE ---" + ColorCode.RESET);
        int numPlayers = state.getPlayerStates().size(); //CHANGED: it was getTurnOrder().size()
        List<String> turnOrder = state.getTurnOrder();   // can be partial or empty

        List<String[]> allTileLines = new ArrayList<>();
        // Let's calculate how many players have already moved (and therefore how many slots at the top are empty)
        int emptySlots = numPlayers - turnOrder.size();

        for (int pos = 1; pos <= numPlayers; pos++) {
            PlayerState player = null;
            // If the position we are drawing is greater than the empty slots,
            // It means that from here on down there are players still waiting.
            if (pos > emptySlots) {
                // Let's phase the index of the list to skip the slots already emptied!
                int listIndex = pos - emptySlots - 1;
                String nickname = turnOrder.get(listIndex);
                player = state.getPlayerStateByNickname(nickname);
            }
            allTileLines.add(getTileLines(pos, numPlayers, player));
        }

        for (int lineIdx = 0; lineIdx < TILE_HEIGHT; lineIdx++) {
            StringBuilder row = new StringBuilder();
            for (String[] tileLines : allTileLines) {
                row.append(tileLines[lineIdx]).append("  ");
            }
            System.out.println(row);
        }
        System.out.println();
    }

    /**
     * Get tile lines string [ ].
     *
     * @param position   the position
     * @param numPlayers the num players
     * @param player     the player
     * @return the string [ ]
     */
    public static String[] getTileLines(int position, int numPlayers, PlayerState player) {
        String color = ColorCode.getTurnTileColor();
        String R     = ColorCode.RESET;
        String B     = ColorCode.BOLD;

        String[] lines = new String[TILE_HEIGHT];

        // Row 0: Top edge
        lines[0] = color + " ." + "-".repeat(W) + "." + R;

        // Row 1: Centered Position
        String title = "POS " + position;
        lines[1] = color + " | " + B + CLIFormatter.center(title, W - 2) + R + color + " |" + R;

        // Line 2: Separator
        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        // Row 3: Player or FREE
        if (player != null) {
            String playerName = ColorCode.playerName(player);
            int nameLen = player.getNickname().length();
            lines[3] = color + " | " + CLIFormatter.centerAnsi(playerName + R + color, nameLen, W - 2) + " |" + R;
        } else {
            String free = R + ColorCode.ITALIC + "FREE" + R + color;
            lines[3] = color + " | " + CLIFormatter.centerAnsi(free, "FREE".length(), W - 2) + " |" + R;
        }

        // Line 4: Separator
        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // Lines 5-6: effects (food and pp), only if different from 0
        String[] attrs = getAttributes(position, numPlayers);
        for (int i = 5; i < 7; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + CLIFormatter.fitLeft(text, W - 2) + " |" + R;
        }

        // Row 7: Bottom Edge
        lines[7] = color + " '" + "-".repeat(W) + "'" + R;

        return lines;
    }

    private static String[] getAttributes(int position, int numPlayers) {
        int food = BoardDictionary.getTurnTileFood(position, numPlayers);
        int pp   = BoardDictionary.getTurnTilePP(position, numPlayers);

        List<String> attrs = new ArrayList<>();

        if (food > 0) attrs.add("+" + food + " Food");
        if (food < 0 && pp < 0) attrs.add("-" + Math.abs(food) + " Food / -" + Math.abs(pp) + " PP");
        else if (food < 0) attrs.add("-" + Math.abs(food) + " Food");
        else if (pp   < 0) attrs.add("-" + Math.abs(pp)   + " PP");

        return attrs.toArray(new String[0]);
    }

}