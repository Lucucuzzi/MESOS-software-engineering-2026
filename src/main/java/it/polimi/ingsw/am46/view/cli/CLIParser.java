package it.polimi.ingsw.am46.view.cli;

/**
 * CLIParser — Converts input strings into structured Command objects.
 *
 * USAGE:
 * Command cmd = parser.parseCommand("move A");
 * if (cmd != null) {
 *     controller.onMoveTotem(cmd.params[0]);
 * }
 *
 * SUPPORTED COMMAND FORMATS:
 * move <tileId>  → action="move", params=["A"]
 * addCard <cardId> → action="addCard", params=["5"]
 * skip           → action="skip", params=[]
 * status         → action="status", params=[]
 * board          → action="board", params=[]
 * help           → action="help", params=[]
 * quit           → action="quit", params=[]
 */
public class CLIParser {

    /**
     * Represents a parsed command.
     */
    public static class Command {
        public final String action;      // "move", "addCard", "skip", etc.
        public final String[] params;    // parameters (could be empty)

        public Command(String action, String[] params) {
            this.action = action;
            this.params = params;
        }

        @Override
        public String toString() {
            return "Command{action='" + action + "', params=" + java.util.Arrays.toString(params) + "}";
        }
    }

    /**
     * Parses an input string into a Command.
     *
     * @param input the string to parse (e.g: "move A", "addCard 5")
     * @return Command if valid and well-formatted, null if invalid
     *
     * EXAMPLES:
     * "move A" → Command("move", ["A"])
     * "addCard 5" → Command("addCard", ["5"])
     * "skip" → Command("skip", [])
     * "movi" → null (unknown command)
     * "move" → null (missing parameter)
     * "move A B" → null (too many parameters)
     */
    public Command parseCommand(String input) {
        // Input validation
        if (input == null || input.isBlank()) {
            return null;
        }

        // Split by spaces
        String[] parts = input.trim().split("\\s+");
        String action = parts[0].toLowerCase();

        // Extract parameters (everything after first token)
        String[] params = new String[parts.length - 1];
        for (int i = 0; i < params.length; i++) {
            params[i] = parts[i + 1];
        }

        // Validate that parameter count is correct
        if (!isValidCommand(action, params.length)) {
            return null;
        }

        return new Command(action, params);
    }

    /**
     * Validates that command has correct number of parameters.
     *
     * @param action the action name
     * @param paramCount the number of parameters received
     * @return true if command is valid, false otherwise
     */
    private boolean isValidCommand(String action, int paramCount) {
        return switch (action) {
            // Commands requiring 1 parameter
            case "move", "add" , "infocard" -> paramCount == 1;

            // Commands requiring 0 parameters
            case "skip", "status", "board", "help", "quit", "playerstats" -> paramCount == 0;

            // Unknown command
            default -> false;
        };
    }
}


