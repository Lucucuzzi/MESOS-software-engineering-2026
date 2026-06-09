package it.polimi.ingsw.am46.view.cli.utils.color;

import it.polimi.ingsw.am46.server.model.Color;
import it.polimi.ingsw.am46.network.dto.PlayerState;

/**
 * Utility to colorize output with ANSI codes.
 * <p>
 * USAGE:
 * System.out.println(ColorCode.error("Error!"));
 * System.out.println(ColorCode.player("Luca", 0));
 */
public class ColorCode {

    /**
     * The constant RESET.
     */
    public static final String RESET = "\u001B[0m";
    /**
     * The constant BOLD.
     */
    public static final String BOLD = "\u001B[1m";

    /**
     * The constant BRIGHT_RED.
     */
// Bright colors (HIGH INTENSITY)
    public static final String BRIGHT_RED = "\u001B[91m";
    /**
     * The constant BRIGHT_GREEN.
     */
    public static final String BRIGHT_GREEN = "\u001B[92m";
    /**
     * The constant BRIGHT_YELLOW.
     */
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    /**
     * The constant BRIGHT_CYAN.
     */
    public static final String BRIGHT_CYAN = "\u001B[96m";

    /**
     * The constant BRIGHT_PURPLE.
     */
    public static final String BRIGHT_PURPLE = "\u001B[95m";

    /**
     * The constant ORANGE.
     */
    public static final String ORANGE = "\u001B[38;5;208m";
    /**
     * The constant DARK_BROWN.
     */
    public static final String DARK_BROWN = "\u001B[38;5;94m";
    /**
     * The constant LIGHT_BROWN.
     */
    public static final String LIGHT_BROWN = "\u001B[38;5;137m";
    /**
     * The constant BRIGHT_BLUE.
     */
    public static final String BRIGHT_BLUE = "\u001B[94m";

    /**
     * The constant WHITE.
     */
    public static final String WHITE = "\u001B[97m";
    /**
     * The constant GOLD.
     */
    public static final String GOLD = "\u001B[38;5;220m";
    /**
     * The constant PURE_YELLOW.
     */
    public static final String PURE_YELLOW = "\u001B[38;5;226m";
    /**
     * The constant ITALIC.
     */
    public static final String ITALIC = "\u001B[3m";

    private ColorCode() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get tile color string.
     *
     * @return the string
     */
    public static String getTileColor(){
        return BRIGHT_GREEN;
    }

    /**
     * Get turn tile color string.
     *
     * @return the string
     */
    public static String getTurnTileColor(){
        return GOLD;
    }

    /**
     * Gets card color.
     *
     * @param type the type
     * @return the card color
     */
    public static String getCardColor(String type) {
        if (type == null) return BRIGHT_CYAN;

        return switch (type.toUpperCase()) {
            case "ARTIST" -> PURE_YELLOW;
            case "HUNTER" -> BRIGHT_RED;
            case "GATHERER" -> ORANGE;
            case "INVENTOR" -> BRIGHT_CYAN;
            case "SHAMAN" -> BRIGHT_PURPLE;
            case "BUILDER" -> DARK_BROWN;
            case "EVENT" -> LIGHT_BROWN;
            case "BUILDING" -> BRIGHT_BLUE;
            default -> RESET;
        };
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
     *
     * @param text  the text
     * @param color the ANSI color code
     * @return the colored text
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    /**
     * Colors text in bold with a color.
     *
     * @param text  the text
     * @param color the color
     * @return the string
     */
    public static String colorizeBold(String text, String color) {
        return BOLD + color + text + RESET;
    }

    /**
     * Colors in red (for errors).
     *
     * @param text the text
     * @return the string
     */
    public static String error(String text) {
        return colorize(text, BRIGHT_RED);
    }

    /**
     * Colors in green (for success).
     *
     * @param text the text
     * @return the string
     */
    public static String success(String text) {
        return colorize(text, BRIGHT_GREEN);
    }

    /**
     * Colors in yellow (for warnings).
     *
     * @param text the text
     * @return the string
     */
    public static String warning(String text) {
        return colorize(text, BRIGHT_YELLOW);
    }

    /**
     * Colors in cyan (for information).
     *
     * @param text the text
     * @return the string
     */
    public static String info(String text) {
        return colorize(text, BRIGHT_CYAN);
    }


    /**
     * Player name string.
     *
     * @param player the player
     * @return the string
     */
    public static String playerName(PlayerState player) {
        if (player == null || player.getNickname() == null) {
            return "Unknown";
        }
        return colorize(player.getNickname(), getAnsiCodePerPlayer(player.getColor()));
    }
}
