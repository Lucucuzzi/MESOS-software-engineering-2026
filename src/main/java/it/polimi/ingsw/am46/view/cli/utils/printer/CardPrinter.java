package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.utils.CardDictionary;
import it.polimi.ingsw.am46.view.cli.utils.formatter.CLIFormatter;

/**
 * The type Card printer.
 */
public class CardPrinter {

    /**
     * The constant CARD_HEIGHT.
     */
    public static final int CARD_HEIGHT = 10;
    private static final int W = 26;

    /**
     * Prints a single card to the standard output (console).
     * * @param id The unique identifier of the card to print.
     *
     * @param id the id
     */
    public static void printCard(int id) {
        for (String line : getCardLines(id)) System.out.println(line);
    }

    /**
     * Generates the ASCII representation of the card row by row.
     * * @param id The unique identifier of the card.
     *
     * @param id the id
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
        // String.format(): formats the ID to be exactly 3 digits, padding with leading zeros .
        String idStr = "ID: " + String.format("%03d", id);
        String era   = CardDictionary.getCardEra(id);
        lines[1] = color + " | " + idStr + CLIFormatter.pad(idStr, era, W) + era + " |" + R;

        lines[2] = color + " |" + "-".repeat(W) + "|" + R;

        String name = CardDictionary.getCardName(id);
        lines[3] = color + " | " + B + CLIFormatter.center(name, W - 2) + R + color + " |" + R;

        lines[4] = color + " |" + "-".repeat(W) + "|" + R;

        // CHANGED: Attributes now occupy rows 5-8 (4 rows instead of 3)
        String[] attrs = getAttributes(id, type);
        for (int i = 5; i < 9; i++) {
            String text = (i - 5 < attrs.length) ? attrs[i - 5] : "";
            lines[i] = color + " | " + CLIFormatter.fitLeft(text, W - 2) + " |" + R;
        }

        lines[9] = color + " '" + "-".repeat(W) + "'" + R; //CHANGED: it was lines[8]

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
                String[] effectLines = CLIFormatter.wrapToLines(detail, 3,W);
                yield new String[]{ costAndPP, effectLines[0], effectLines[1], effectLines[2] };
            }
            case "EVENT"  -> CLIFormatter.wrapToLines(detail, 3, W);
            default       -> detail.isBlank() ? new String[]{} : detail.split("\n");
        };
    }

}