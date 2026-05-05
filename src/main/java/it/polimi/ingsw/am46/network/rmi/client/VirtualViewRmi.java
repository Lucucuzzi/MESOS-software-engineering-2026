package it.polimi.ingsw.am46.network.rmi.client;

import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
     */
    void updateView(GameState gameState)
            throws RemoteException;

    /* The server sends a specific error to this client.
       E.g.: “It's not your turn!”, “Not enough food!” */
    void signalError(String errorMessage)
            throws RemoteException;

     //The server notifies the end of the game with winners and final scores.

    void showWinner(GameState finalState)
            throws RemoteException;
    void ping() throws RemoteException;

    void abortGame(String message) throws RemoteException;

}
