package it.polimi.ingsw.am46.network;

import it.polimi.ingsw.am46.network.dto.GameState;

import java.rmi.RemoteException;

/**
 * The interface Virtual view.
 */
public interface VirtualView {
    /*
      Base interface that defines the notifications the server
      can send to clients. Independent of network technology.
      Implemented by the server-side RMI or Socket layer.
      This is the Broadcast Manager — it manages the list of registered CURs.
     */

    /**
     * Register client.
     *
     * @param nickname the nickname
     * @param cur      the cur
     * @throws Exception the exception
     */
/*
     Registers a new client (ClientUpdateReceiver) associated
     with the nickname. Called by ServerController during connect().
     */
    void registerClient(String nickname, NetworkMode cur)
            throws Exception;


     //Removes a client from the list (disconnection).

    /**
     * Unregister client.
     *
     * @param nickname the nickname
     */
    void unregisterClient(String nickname);

    /**
     * Broadcast update.
     *
     * @param gameState the game state
     * @throws Exception the exception
     */
/*
     Sends the updated gameState to ALL registered clients.
     Called by ServerController after every valid action.
     */
    void broadcastUpdate(GameState gameState) throws Exception;

    /**
     * Send error.
     *
     * @param nickname     the nickname
     * @param errorMessage the error message
     * @throws Exception the exception
     */
/*
     Sends an error message ONLY to the specified client.
     Called by ServerController when Game throws
     IllegalStateException.
     */
    void sendError(String nickname, String errorMessage)
            throws Exception;

    /**
     * Broadcast error.
     *
     * @param errorMessage the error message
     * @throws Exception the exception
     */
    void broadcastError(String errorMessage) throws Exception;

    /**
     * Broadcast abort.
     *
     * @param message the message
     * @throws Exception the exception
     */
    void broadcastAbort(String message) throws Exception;


     //Sends the final screen with winners and scores to ALL clients.

    /**
     * Broadcast winner.
     *
     * @param finalState the final state
     * @throws Exception the exception
     */
    void broadcastWinner(GameState finalState) throws Exception;

    /**
     * Clear clients.
     */
    void clearClients();
}
