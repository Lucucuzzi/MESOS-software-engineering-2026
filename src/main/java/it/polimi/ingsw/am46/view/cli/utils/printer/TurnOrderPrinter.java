package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.utils.BoardDictionary;

import java.util.ArrayList;
import java.util.List;

public class TurnOrderPrinter {

    public static final int TILE_HEIGHT = 8;
    private static final int W = 17;

    public static void printTurnOrder(GameState state) {
        int numPlayers = state.getPlayerStates().size(); // CAMBIATO: era getTurnOrder().size()
        List<String> turnOrder = state.getTurnOrder();   // può essere parziale o vuota

        List<String[]> allTileLines = new ArrayList<>();
        for (int pos = 1; pos <= numPlayers; pos++) {
            // CAMBIATO: il giocatore esiste solo se la posizione è già stata assegnata
            PlayerState player = null;
            if (turnOrder.size() >= pos) {
                String nickname = turnOrder.get(pos - 1);
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

    public static String[] getTileLines(int position, int numPlayers, PlayerState player) {
        String color = ColorCode.getTurnTileColor();
        String R     = ColorCode.RESET;
        String B     = ColorCode.BOLD;

        String[] lines = new String[TILE_HEIGHT];

        // Riga 0: bordo superiore
        lines[0] = color + " ." + "-".repeat(W) + "." + R;

        // Riga 1: posizione centrata
        String title = "POS " + position;
        lines[1] = color + " | " + B + center(title, W - 2) + R + color + " |" + R;

        // Riga 2: separatore
        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        // Riga 3: giocatore o FREE
        if (player != null) {
            String playerName = ColorCode.playerName(player);
            int nameLen = player.getNickname().length();
            int pad = Math.max(0, (W - 2 - nameLen) / 2);
            lines[3] = color + " | " + " ".repeat(pad) + playerName + R + color
                    + " ".repeat(Math.max(0, W - 2 - nameLen - pad)) + " |" + R;
        } else {
            String free = R+ ColorCode.ITALIC + "FREE" + R + color;
            int pad = (W - 2 - "FREE".length()) / 2;
            lines[3] = color + " | " + " ".repeat(pad) + free
                    + " ".repeat(W - 2 - "FREE".length() - pad) + " |" + R;
        }

        // Riga 4: separatore
        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // Righe 5-6: effetti (food e pp), solo se diversi da 0
        String[] attrs = getAttributes(position, numPlayers);
        for (int i = 5; i < 7; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + fitLeft(text, W - 2) + " |" + R;
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