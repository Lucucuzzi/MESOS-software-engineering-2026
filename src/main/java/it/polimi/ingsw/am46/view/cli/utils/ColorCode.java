package it.polimi.ingsw.am46.view.cli.utils;

import it.polimi.ingsw.am46.model.Color;
import it.polimi.ingsw.am46.network.dto.PlayerState;

/**
 * Utility to colorize output with ANSI codes.
 *
 * USAGE:
 *   System.out.println(ColorCode.error("Error!"));
 *   System.out.println(ColorCode.player("Luca", 0));
 */
public class ColorCode {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    // Bright colors (HIGH INTENSITY)
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_CYAN = "\u001B[96m";

    private ColorCode() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static String getAnsiCodePerPlayer(Color color) {
        if (color == null) return RESET;
        return switch (color) {
            case RED -> "\u001B[31m";
            case PURPLE -> "\u001B[95m";
            case YELLOW -> "\u001B[33m";
            case WHITE -> "\u001B[97m";
            case BLUE -> "\u001B[94m";
        };
    }

    /**
     * Colors text with the specified color.
     * @param text the text
     * @param color the ANSI color code
     * @return the colored text
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    /**
     * Colors text in bold with a color.
     */
    public static String colorizeBold(String text, String color) {
        return BOLD + color + text + RESET;
    }

    /**
     * Colors in red (for errors).
     */
    public static String error(String text) {
        return colorize(text, BRIGHT_RED);
    }

    /**
     * Colors in green (for success).
     */
    public static String success(String text) {
        return colorize(text, BRIGHT_GREEN);
    }

    /**
     * Colors in yellow (for warnings).
     */
    public static String warning(String text) {
        return colorize(text, BRIGHT_YELLOW);
    }

    /**
     * Colors in cyan (for information).
     */
    public static String info(String text) {
        return colorize(text, BRIGHT_CYAN);
    }


    public static String playerName(PlayerState player) {
        if (player == null || player.getNickname() == null) {
            return "Unknown";
        }
        return colorize(player.getNickname(), getAnsiCodePerPlayer(player.getColor()));
    }
}
