package it.polimi.ingsw.am46.exception;

/**
 * The type Game already started exception.
 */
public class GameAlreadyStartedException extends IllegalStateException {
    /**
     * Instantiates a new Game already started exception.
     *
     * @param message the message
     */
    public GameAlreadyStartedException(String message) {
        super(message);
    }
}