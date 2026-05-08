package it.polimi.ingsw.am46.exception;

public class GameAlreadyStartedException extends IllegalStateException {
    public GameAlreadyStartedException(String message) {
        super(message);
    }
}