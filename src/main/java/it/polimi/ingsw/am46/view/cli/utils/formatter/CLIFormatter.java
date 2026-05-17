package it.polimi.ingsw.am46.view.cli.utils.formatter;

public class CLIFormatter {
    /**
     * Centers a given text within a specific length.
     * If the text is too long, it truncates it. Otherwise, it adds equal spaces to the left and right.
     * * @param text The string to be centered.
     * @param len  The total width available for the text.
     * @return The centered string.
     */
    public static String center(String text, int len) {
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
    public static String fitLeft(String text, int len) {
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
    public static String pad(String left, String right, int W) {
        int spaces = (W - 2) - left.length() - right.length();
        return spaces > 0 ? " ".repeat(spaces) : " ";
    }

    /**
     * Wraps a long string of text into a predefined number of lines without breaking words in the middle.
     * * @param text     The long string to wrap.
     * @param maxLines The maximum number of lines allowed.
     * @return An array of Strings, each representing a fitted line of text.
     */
    public static String[] wrapToLines(String text, int maxLines, int W) {
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

    /**
     * Centers a string that contains ANSI codes, where visualLen is the visible length
     * (without escape codes) and W is the total available width.
     */
    public static String centerAnsi(String ansiText, int visualLen, int totalWidth) {
        int pad = Math.max(0, (totalWidth - visualLen) / 2);
        int rightPad = Math.max(0, totalWidth - visualLen - pad);
        return " ".repeat(pad) + ansiText + " ".repeat(rightPad);
    }
}
