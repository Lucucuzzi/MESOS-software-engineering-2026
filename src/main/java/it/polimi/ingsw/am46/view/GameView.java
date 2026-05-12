package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.dto.GameState;

/**
 * Interface that defines the contract for all views (CLI, GUI, etc.)
 *
 * GOLDEN RULE: No method here can be technology-specific.
 *
 * Thread-safety: methods of this interface are called from the network thread.
 * Each implementation MUST handle synchronization.
 */
public interface GameView extends ModelObserver {

    /**
     * Starts the view and all its internal threads.
     * This method is asynchronous — returns immediately.
     */
    void start();

    /**
     * Stops the view and releases all resources.
     * Closes scanner, interrupts threads, unlocks main via latch.
     */
    void stop();

    /**
     * Displays a generic message to the user.
     */
    void showMessage(String message);

    /**
     * Draws the game board based on GameState.
     */
    void drawBoard(GameState state);

    default void onAbort(String reason) {
        stop();
    }
}
