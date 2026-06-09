package it.polimi.ingsw.am46.network.rmi.client;

import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Virtual view rmi.
 */
public interface VirtualViewRmi extends Remote, NetworkMode {
    /*
     * RMI implementation of ClientUpdateReceiver.
     * Extends Remote → the server can call these methods
     * on the client over the network as if they were local.
     * Implemented by RmiClient, which extends UnicastRemoteObject.
     * This is what makes the client “reachable” by the server.
     */

    /**
     * The server sends the updated game state to this client.
     * Called by RmiServer.broadcastUpdate() on every client.
     *
     * @param gameState the game state
     * @throws RemoteException the remote exception
     */
    void updateView(GameState gameState)
            throws RemoteException;

    /**
     * Signal error.
     *
     * @param errorMessage the error message
     * @throws RemoteException the remote exception
     */
/* The server sends a specific error to this client.
       E.g.: “It's not your turn!”, “Not enough food!” */
    void signalError(String errorMessage)
            throws RemoteException;

     //The server notifies the end of the game with winners and final scores.

    /**
     * Show winner.
     *
     * @param finalState the final state
     * @throws RemoteException the remote exception
     */
    void showWinner(GameState finalState)
            throws RemoteException;

    /**
     * Ping.
     *
     * @throws RemoteException the remote exception
     */
    void ping() throws RemoteException;

    /**
     * Abort game.
     *
     * @param message the message
     * @throws RemoteException the remote exception
     */
    void abortGame(String message) throws RemoteException;

}
