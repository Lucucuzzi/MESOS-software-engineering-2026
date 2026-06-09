package it.polimi.ingsw.am46.network;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;

import java.util.List;

/**
 * The interface Virtual server.
 *
 * @param <T> the type parameter
 */
public interface VirtualServer<T> {
    /*
     * Base interface that defines the commands the client
     * can send to the server. Independent of the network technology.
     * With RMI → VirtualServerRmi extends Remote, VirtualServer
     * With Socket → VirtualServerSocket extends VirtualServer
     */


    /**
     * The client connects to the game by passing its
     * nickname and a reference to its ClientUpdateReceiver.
     * The server registers it as an observer in the VirtualView.
     *
     * @param nickname  the nickname
     * @param colorName the color name
     * @param cur       the cur
     * @throws Exception the exception
     */
    void connect(String nickname,String colorName, T cur) throws Exception;

    /**
     * Sets expected players.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     * @throws Exception the exception
     */
    void setExpectedPlayers(String nickname, int numPlayers) throws Exception;
    // By using Generics T , we are telling the system : accept anything as a receiver
    // in this way, the interface is independent of the network technology.

    /**
     * Move totem.
     *
     * @param nickname    the nickname
     * @param offerTileId the offer tile id
     * @throws Exception the exception
     */
/*
     The client wants to place the totem on an Offer tile.
     The server validates: is it the right turn? Is the tile free?
     */
    void moveTotem(String nickname, String offerTileId) throws Exception;

    /**
     * Add card.
     *
     * @param nickname the nickname
     * @param cardId   the card id
     * @throws Exception the exception
     */
/*
     The client wants to take a card from the board.
     The server validates: turn, phase, food, card available.
     */
    void addCard(String nickname, String cardId) throws Exception;

    /**
     * Add extra card.
     *
     * @param nickname the nickname
     * @param cardId   the card id
     * @throws Exception the exception
     */
/*
     The client wants to draw the optional extra card
     (only if they have the special building).
     cardId == null means a voluntary skip.
     */
    void addExtraCard(String nickname, String cardId) throws Exception;


    /**
     * Skip extra draw.
     *
     * @param nickname the nickname
     * @throws Exception the exception
     */
//The client explicitly skips the ExtraDraw phase
    void skipExtraDraw(String nickname) throws Exception;

    /**
     * Gets available colors.
     *
     * @return the available colors
     * @throws Exception the exception
     */
    List<String> getAvailableColors() throws Exception;

    /**
     * Reconnect.
     *
     * @param nickname the nickname
     * @param cur      the cur
     * @throws Exception the exception
     */
    void reconnect(String nickname, T cur) throws Exception;

    /**
     * Gets leaderboard.
     *
     * @param numPlayers the num players
     * @return the leaderboard
     * @throws Exception the exception
     */
    List<LeaderboardEntry> getLeaderboard(int numPlayers) throws Exception;

    /**
     * Gets player position.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     * @return the player position
     * @throws Exception the exception
     */
    int getPlayerPosition(String nickname, int numPlayers) throws Exception;
}
