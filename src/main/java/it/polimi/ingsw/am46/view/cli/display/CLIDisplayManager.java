package it.polimi.ingsw.am46.view.cli.display;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.cli.utils.ColorCode;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CLIDisplayManager — Manages ONLY display and formatting.
 *
 * RESPONSIBILITIES:
 * - Print welcome screen
 * - Print game board
 * - Print menu
 * - Print status and help messages
 * - Synchronize stdout access with screenLock
 *
 * DOES NOT:
 * - Read input
 * - Manage threading
 * - Parse commands
 *
 * THREAD-SAFETY:
 * Every public method acquires screenLock.writeLock() before printing.
 * This ensures network thread and input thread don't write to screen simultaneously.
 */
public class CLIDisplayManager {

    private final ReentrantReadWriteLock screenLock;
    private final LocalModel localModel;

    public CLIDisplayManager(ReentrantReadWriteLock screenLock, LocalModel localModel) {
        this.screenLock = screenLock;
        this.localModel = localModel;
    }

    /**
     * Prints the welcome screen.
     */
    public void displayWelcome() {
        screenLock.writeLock().lock();
        try {
            System.out.println(ColorCode.BRIGHT_CYAN +
                    "======================\n" +
                    "       MESOS \n" +
                    "======================" + ColorCode.RESET);
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints the commands menu.
     */
    public void displayHelp() {
        screenLock.writeLock().lock();
        try {
            System.out.println(ColorCode.BOLD + "Available commands:" + ColorCode.RESET);
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "move <tileId>" + ColorCode.RESET +
                    " — Move the totem");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "add <cardId>" + ColorCode.RESET +
                    " — Add a card");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "skip" + ColorCode.RESET +
                    " — Skip your extra turn");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "board" + ColorCode.RESET +
                    " — Show the board");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "status" + ColorCode.RESET +
                    " — Show the status");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "help" + ColorCode.RESET +
                    " — Show this menu");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "quit" + ColorCode.RESET +
                    " — Quit the game");
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints the game board.
     *
     * TODO: Implement based on actual GameState structure.
     * For now it's a stub showing basic information.
     */
    public void displayBoard(GameState state) {
        if (state == null) {
            displayMessage(ColorCode.warning("⚠️  Status not available"));
            return;
        }

        screenLock.writeLock().lock();
        try {
            System.out.println();
            System.out.println(ColorCode.BRIGHT_CYAN + "==== BOARD ====" + ColorCode.RESET);
            System.out.println(ColorCode.info("Round: ") + state.getRound());
            System.out.println(ColorCode.info("Turn: ") + ColorCode.playerName(state.getActivePlayerState()));
            System.out.println(ColorCode.info("Phase: ") + state.getCurrentPhaseName());
            System.out.println();
            // TODO: print tiles, cards, etc.
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints quick game status.
     */
    public void displayStatus(GameState state) {
        if (state == null) {
            displayMessage(ColorCode.warning("⚠️  Status not available"));
            return;
        }

        screenLock.writeLock().lock();
        try {
            System.out.println();
            System.out.println(ColorCode.info("📊 Quick status:"));
            System.out.println(ColorCode.info("Round: " + state.getRound()));
            System.out.println(ColorCode.info("Turn: ") + ColorCode.playerName(state.getActivePlayerState()));
            System.out.println(ColorCode.info("Phase: " + state.getCurrentPhaseName()));
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints a generic message.
     * Called by CLIInputHandler to send feedback.
     */
    public void displayMessage(String message) {
        screenLock.writeLock().lock();
        try {
            System.out.println(message);
        } finally {
            screenLock.writeLock().unlock();
        }
    }
}