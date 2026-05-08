package it.polimi.ingsw.am46.exception;

public class InvalidConnectionException extends IllegalArgumentException {
    public InvalidConnectionException(String message) {
        super(message);
    }
}