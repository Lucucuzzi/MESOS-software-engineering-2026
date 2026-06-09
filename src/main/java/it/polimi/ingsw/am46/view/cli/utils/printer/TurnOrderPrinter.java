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
        int numPlayers = state.getPlayerStates().size(); // CAMBIATO: era getTurnOrder().size()
        List<String> turnOrder = state.getTurnOrder();   // può essere parziale o vuota

        List<String[]> allTileLines = new ArrayList<>();
        // Calcoliamo quanti giocatori hanno già mosso (e quindi quanti slot in cima sono vuoti)
        int emptySlots = numPlayers - turnOrder.size();

        for (int pos = 1; pos <= numPlayers; pos++) {
            PlayerState player = null;
            // Se la posizione che stiamo disegnando è maggiore degli slot vuoti,
            // vuol dire che da qui in giù ci sono i giocatori ancora in attesa.
            if (pos > emptySlots) {
                // Sfasiamo l'indice della lista per saltare gli slot già svuotati!
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

        // Riga 0: bordo superiore
        lines[0] = color + " ." + "-".repeat(W) + "." + R;

        // Riga 1: posizione centrata
        String title = "POS " + position;
        lines[1] = color + " | " + B + CLIFormatter.center(title, W - 2) + R + color + " |" + R;

        // Riga 2: separatore
        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        // Riga 3: giocatore o FREE
        if (player != null) {
            String playerName = ColorCode.playerName(player);
            int nameLen = player.getNickname().length();
            lines[3] = color + " | " + CLIFormatter.centerAnsi(playerName + R + color, nameLen, W - 2) + " |" + R;
        } else {
            String free = R + ColorCode.ITALIC + "FREE" + R + color;
            lines[3] = color + " | " + CLIFormatter.centerAnsi(free, "FREE".length(), W - 2) + " |" + R;
        }

        // Riga 4: separatore
        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // Righe 5-6: effetti (food e pp), solo se diversi da 0
        String[] attrs = getAttributes(position, numPlayers);
        for (int i = 5; i < 7; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + CLIFormatter.fitLeft(text, W - 2) + " |" + R;
        }

        // Riga 7: bordo inferiore
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