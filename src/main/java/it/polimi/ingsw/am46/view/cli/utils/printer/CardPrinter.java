package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.utils.CardDictionary;

public class CardPrinter {

    public static final int CARD_HEIGHT = 10;
    private static final int W = 26;

    /**
     * Prints a single card to the standard output (console).
     * * @param id The unique identifier of the card to print.
     */
    public static void printCard(int id) {
        for (String line : getCardLines(id)) System.out.println(line);
    }

    /**
     * Generates the ASCII representation of the card row by row.
     * * @param id The unique identifier of the card.
     * @return An array of Strings, where each String represents a horizontal line of the card.
     */
    public static String[] getCardLines(int id) {
        String type  = CardDictionary.getCardType(id);
        String color = ColorCode.getCardColor(type);
        String R     = ColorCode.RESET;
        String B     = ColorCode.BOLD;

        String[] lines = new String[CARD_HEIGHT];

        // String.repeat(n): creates a new string by repeating the "-" character 'W' times.
        lines[0] = color + " ." + "-".repeat(W) + "." + R;
        // String.format(): formats the ID to be exactly 3 digits, padding with leading zeros (e.g., "005").
        String idStr = "ID: " + String.format("%03d", id);
        String era   = CardDictionary.getCardEra(id);
        lines[1] = color + " | " + idStr + pad(idStr, era) + era + " |" + R;

        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        String name = CardDictionary.getCardName(id);
        lines[3] = color + " | " + B + center(name, W - 2) + R + color + " |" + R;

        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // CAMBIATO: ora gli attributi occupano righe 5-8 (4 righe invece di 3)
        String[] attrs = getAttributes(id, type);
        for (int i = 5; i < 9; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + fitLeft(text, W - 2) + " |" + R;
        }

        lines[9] = color + " '" + "-".repeat(W) + "'" + R; // CAMBIATO: era lines[8]

        return lines;
    }

    /**
     * Retrieves and formats the card's details and effects based on its type.
     * Buildings get a special format including Cost and Prestige Points (PP).
     * * @param id   The unique identifier of the card.
     * @param type The type of the card (e.g., "BUILDING", "EVENT").
     * @return An array of Strings containing the formatted attribute lines.
     */
    private static String[] getAttributes(int id, String type) {
        String detail = CardDictionary.getCardDetail(id);
        return switch (type) {
            case "BUILDING" -> {
                String costAndPP = "Cost: " + CardDictionary.getBuildingCost(id)
                        + "  |  Prestige: " + CardDictionary.getBuildingPP(id) + " PP";
                String[] effectLines = wrapToLines(detail, 3);
                yield new String[]{ costAndPP, effectLines[0], effectLines[1], effectLines[2] };
            }
            case "EVENT"  -> wrapToLines(detail, 3);
            default       -> detail.isBlank() ? new String[]{} : detail.split("\n");
        };
    }

    /**
     * Centers a given text within a specific length.
     * If the text is too long, it truncates it. Otherwise, it adds equal spaces to the left and right.
     * * @param text The string to be centered.
     * @param len  The total width available for the text.
     * @return The centered string.
     */
    private static String center(String text, int len) {
        // String.substring(begin, end): extracts a portion of the string from index 0 up to 'len', cutting off the excess.
        if (text.length() >= len) return text.substring(0, len);
        int left = (len - text.length()) / 2;
        return " ".repeat(left) + text + " ".repeat(len - text.length() - left);
    }

    /**
     * Aligns the text to the left within a specific length, padding the right side with spaces.
     * Truncates the text if it exceeds the specified length.
     * * @param text The string to align.
     * @param len  The total width available.
     * @return The left-aligned string padded with spaces.
     */
    private static String fitLeft(String text, int len) {
        if (text == null) text = "";
        if (text.length() > len) return text.substring(0, len);
        return text + " ".repeat(len - text.length());
    }

    /**
     * Calculates and returns the exact number of blank spaces needed between a left-aligned
     * string and a right-aligned string to push the right string to the edge of the card.
     * * @param left  The string positioned on the left (e.g., "ID: 001").
     * @param right The string positioned on the right (e.g., "Era 1").
     * @return A string containing the calculated number of spaces.
     */
    private static String pad(String left, String right) {
        int spaces = (W - 2) - left.length() - right.length();
        return spaces > 0 ? " ".repeat(spaces) : " ";
    }

    /**
     * Wraps a long string of text into a predefined number of lines without breaking words in the middle.
     * * @param text     The long string to wrap.
     * @param maxLines The maximum number of lines allowed.
     * @return An array of Strings, each representing a fitted line of text.
     */
    private static String[] wrapToLines(String text, int maxLines) {
        if (text == null || text.isEmpty()) return new String[]{};
        // String.split(" "): creates an array of individual words by splitting the text at every space character.
        String[] words = text.split(" ");
        String[] result = new String[maxLines];
        int line = 0;
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (line >= maxLines) break;
            // +1 because we count the space
            if (sb.length() + (sb.length() > 0 ? 1 : 0) + word.length() <= W - 2) {
                // StringBuilder.append(): efficiently adds the space or word to the end of the current sequence in memory
                if (sb.length() > 0) sb.append(' ');
                sb.append(word);
            } else {
                result[line++] = sb.toString();
                sb = new StringBuilder(word);
            }
        }
        if (line < maxLines && sb.length() > 0) result[line] = sb.toString();
        for (int i = 0; i < maxLines; i++) if (result[i] == null) result[i] = "";
        return result;
    }
}