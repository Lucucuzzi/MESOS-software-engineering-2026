package it.polimi.ingsw.am46.view.cli.handler;

import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.cli.CLIParser;
import it.polimi.ingsw.am46.view.cli.utils.ColorCode;
import java.util.function.Consumer;

/**
 * CLIInputHandler — Manages ONLY user input.
 *
 * RESPONSIBILITIES:
 * - Read input strings (provided by CLIView)
 * - Parse commands
 * - Validate format
 * - Execute commands (delegate to ClientController)
 * - Send feedback to user (via callbacks)
 *
 * DOES NOT:
 * - Print directly — uses callbacks
 * - Manage threading — CLIView manages executor
 * - Contain display logic
 *
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

    //To prevent threads from saving the variable in cache, it must be an always-updated variable
    private volatile boolean running = true;

    /**
     * Constructor.
     */
    public CLIInputHandler(ClientController controller) {
        this.controller = controller;
        this.parser = new CLIParser();
    }

    // ============================================================
    // CALLBACK SETTERS
    // ============================================================

    // in displayCallBack now is saved a message that we want to show
    // it will use by CliView
    public void setDisplayCallback(Consumer<String> callback) {
        this.displayCallback = callback;
    }

    //in onStatusRequest now is saved a Runnable that we want to run when the user will write status
    // it will use by CliView
    public void setOnStatusRequested(Runnable callback) {
        this.onStatusRequested = callback;
    }

    public void setOnBoardRequested(Runnable callback) {
        this.onBoardRequested = callback;
    }

    public void setOnHelpRequested(Runnable callback) {
        this.onHelpRequested = callback;
    }

    public void setOnQuitRequested(Runnable callback) {
        this.onQuitRequested = callback;
    }

    // ============================================================
    // INPUT PROCESSING
    // ============================================================

    /**
     * Parse the command and execute it. Provides feedback via callbacks.
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
            showError("Comando non valido. Digita 'help' per vedere i comandi.");
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
            default -> showError("Comando sconosciuto: " + cmd.action);
        }
    }

    // ============================================================
    // COMMAND EXECUTORS
    // ============================================================

    // to execute a command, we call the appropriate method on ClientController.
    private void executeMoveCommand(CLIParser.Command cmd) {
        String tileId = cmd.params[0];
        try {
            controller.onMoveTotem(tileId);
            showSuccess("Comando move inviato");
        } catch (Exception e) {
            showError("Errore move: " + e.getMessage());
        }
    }

    private void executeAddCommand(CLIParser.Command cmd) {
        String cardId = cmd.params[0];
        try {
            controller.onAddCard(cardId);
            showSuccess("Comando add inviato");
        } catch (Exception e) {
            showError("Errore add: " + e.getMessage());
        }
    }

    private void executeSkipCommand() {
        try {
            controller.onSkipExtraDraw();
            showSuccess("Turno saltato");
        } catch (Exception e) {
            showError("Errore skip: " + e.getMessage());
        }
    }
    // to execute this command, we just call the callback to display the status screen.
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

    // ============================================================
    // MESSAGING HELPERS
    // ============================================================
    //.accept(...) take the string and pass it to the function that it is saved is it and execute it
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

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}