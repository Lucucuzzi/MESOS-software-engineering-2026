package it.polimi.ingsw.am46.view.cli.handler;

import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.cli.CLIParser;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import java.util.function.Consumer;

/**
 * CLIInputHandler — Manages ONLY user input.
 * <p>
 * RESPONSIBILITIES:
 * - Read input strings (provided by CLIView)
 * - Parse commands
 * - Validate format
 * - Execute commands (delegate to ClientController)
 * - Send feedback to user (via callbacks)
 * <p>
 * DOES NOT:
 * - Print directly — uses callbacks
 * - Manage threading — CLIView manages executor
 * - Contain display logic
 * <p>
 * CALLBACKS:
 * Communicates with CLIDisplayManager exclusively through callbacks.
 * This avoids direct coupling.
 */
public class CLIInputHandler {

    private final ClientController controller;
    private final CLIParser parser;

    // ============================================================
    // CALLBACKS — communication to CLIDisplayManager
    // ============================================================

    private Consumer<String> displayCallback;       // Send messages to display
    private Runnable onStatusRequested;             // User typed "status"
    private Runnable onBoardRequested;              // User typed "board"
    private Runnable onHelpRequested;               // User typed "help"
    private Runnable onQuitRequested;               // User typed "quit"
    private Runnable onPlayerStatsRequested;
    private Consumer<String> onInfoRequested; // Takes the card ID

    // To prevent threads from saving the variable in cache, it must be an always-updated variable
    private volatile boolean running = true;

    /**
     * Constructor.
     *
     * @param controller the controller
     */
    public CLIInputHandler(ClientController controller) {
        this.controller = controller;
        this.parser = new CLIParser();
    }

    // ============================================================
    // CALLBACK SETTERS
    // ============================================================

    /**
     * Sets display callback.
     *
     * @param callback the callback
     */
// In displayCallback now is saved a message that we want to show
    // It will be used by CliView
    public void setDisplayCallback(Consumer<String> callback) {
        this.displayCallback = callback;
    }

    /**
     * Sets on status requested.
     *
     * @param callback the callback
     */
// In onStatusRequested now is saved a Runnable that we want to run when the user types "status"
    // It will be used by CliView
    public void setOnStatusRequested(Runnable callback) {
        this.onStatusRequested = callback;
    }

    /**
     * Sets on board requested.
     *
     * @param callback the callback
     */
    public void setOnBoardRequested(Runnable callback) {
        this.onBoardRequested = callback;
    }

    /**
     * Sets on help requested.
     *
     * @param callback the callback
     */
    public void setOnHelpRequested(Runnable callback) {
        this.onHelpRequested = callback;
    }

    /**
     * Sets on quit requested.
     *
     * @param callback the callback
     */
    public void setOnQuitRequested(Runnable callback) {
        this.onQuitRequested = callback;
    }

    /**
     * Sets on player stats requested.
     *
     * @param callback the callback
     */
    public void setOnPlayerStatsRequested(Runnable callback) { this.onPlayerStatsRequested = callback; }

    /**
     * Sets on info requested.
     *
     * @param callback the callback
     */
    public void setOnInfoRequested(Consumer<String> callback) { this.onInfoRequested = callback; }

    // ============================================================
    // INPUT PROCESSING
    // ============================================================

    /**
     * Parse the command and execute it. Provides feedback via callbacks.
     *
     * @param input the input
     */
    public void handleInput(String input) {
        // Check if user wants to quit
        if (input.equalsIgnoreCase("quit")) {
            running = false;
            if (onQuitRequested != null) {
                onQuitRequested.run();
            }
            return;
        }

        // Parse the command
        CLIParser.Command cmd = parser.parseCommand(input);
        if (cmd == null) {
            showError("Invalid command. Type 'help' to see the commands.");
            return;
        }

        // Execute the command
        handleCommand(cmd);
    }

    /**
     * Executes an already parsed command.
     */
    private void handleCommand(CLIParser.Command cmd) {
        switch (cmd.action.toLowerCase()) {
            case "move" -> executeMoveCommand(cmd);
            case "add" -> executeAddCommand(cmd);
            case "skip" -> executeSkipCommand();
            case "status" -> executeStatusCommand();
            case "board" -> executeBoardCommand();
            case "help" -> executeHelpCommand();
            case "playerstats" -> executePlayerStatsCommand();
            case "infocard" -> executeInfoCommand(cmd);
            default -> showError("Unknown command: " + cmd.action);
        }
    }

    // ============================================================
    // COMMAND EXECUTORS
    // ============================================================

    // To execute a command, we call the appropriate method on ClientController.
    private void executeMoveCommand(CLIParser.Command cmd) {
        String tileId = cmd.params[0].toUpperCase();
        try {
            if (controller.onMoveTotem(tileId)) {
                showSuccess("Move command sent");
            }
        } catch (Exception e) {
            showError("Move error: " + e.getMessage());
        }
    }

    private void executeAddCommand(CLIParser.Command cmd) {
        String cardId = cmd.params[0];
        try {
            if (controller.onAddCard(cardId)) {
                showSuccess("Add command sent");
            }
        } catch (Exception e) {
            showError("Add error: " + e.getMessage());
        }
    }

    private void executeSkipCommand() {
        try {
            if (controller.onSkipExtraDraw()) {
                showSuccess("Turn skipped");
            }
        } catch (Exception e) {
            showError("Skip error: " + e.getMessage());
        }
    }

    // To execute this command, we just call the callback to display the status screen.
    // The actual data will be read from LocalModel by CLIDisplayManager.
    private void executeStatusCommand() {
        if (onStatusRequested != null) {
            onStatusRequested.run();
        }
    }

    private void executeBoardCommand() {
        if (onBoardRequested != null) {
            onBoardRequested.run();
        }
    }

    private void executeHelpCommand() {
        if (onHelpRequested != null) {
            onHelpRequested.run();
        }
    }
    private void executePlayerStatsCommand() {
        if (onPlayerStatsRequested != null) onPlayerStatsRequested.run();
    }

    private void executeInfoCommand(CLIParser.Command cmd) {
        if (cmd.params.length == 1 && onInfoRequested != null) {
            onInfoRequested.accept(cmd.params[0]);
        } else {
            showError("Invalid format. Try: infocard <id>");
        }
    }

    // ============================================================
    // MESSAGING HELPERS
    // ============================================================

    // .accept(...) takes the string and passes it to the saved function to execute it
    private void showError(String message) {
        if (displayCallback != null) {
            displayCallback.accept(ColorCode.error(message));
        } else {
            System.err.println(message);
        }
    }

    private void showSuccess(String message) {
        if (displayCallback != null) {
            displayCallback.accept(ColorCode.success(message));
        } else {
            System.out.println(message);
        }
    }

    private void showInfo(String message) {
        if (displayCallback != null) {
            displayCallback.accept(ColorCode.info(message));
        } else {
            System.out.println(message);
        }
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    /**
     * Stop.
     */
    public void stop() {
        running = false;
    }

    /**
     * Is running boolean.
     *
     * @return the boolean
     */
    public boolean isRunning() {
        return running;
    }
}