package it.polimi.ingsw.am46.exception;

/**
 * Thrown by ServerController.connect() when a client tries to
 * connect with a nickname that already exists in the game
 * but is currently marked as offline (disconnected).
 * This is NOT an error — it signals that the client should
 * call reconnect() instead of connect().
 * The client catches this and switches to the reconnect flow.
 */
public class NicknameOfflineException extends Exception {
    private final String nickname;

    /**
     * Instantiates a new Nickname offline exception.
     *
     * @param nickname the nickname
     */
    public NicknameOfflineException(String nickname) {
        super("Player '" + nickname
                + "' exists but is offline. Use reconnect() instead.");
        this.nickname = nickname;
    }


     //The nickname of the offline player trying to reconnect.

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }
}
