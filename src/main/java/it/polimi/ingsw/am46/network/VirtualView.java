package it.polimi.ingsw.am46.network;

import it.polimi.ingsw.am46.network.dto.GameState;

import java.rmi.RemoteException;

public interface VirtualView {
    /*
      Base interface that defines the notifications the server
      can send to clients. Independent of network technology.
      Implemented by the server-side RMI or Socket layer.
      This is the Broadcast Manager — it manages the list of registered CURs.
     */

    /*
     Registers a new client (ClientUpdateReceiver) associated
     with the nickname. Called by ServerController during connect().
     */
    void registerClient(String nickname, Object cur)
            throws Exception;


     //Removes a client from the list (disconnection).

    void unregisterClient(String nickname);

    /*
     Sends the updated gameState to ALL registered clients.
     Called by ServerController after every valid action.
     */
    void broadcastUpdate(GameState gameState) throws Exception;

    /*
     Sends an error message ONLY to the specified client.
     Called by ServerController when Game throws
     IllegalStateException.
     */
    void sendError(String nickname, String errorMessage)
            throws Exception;

    void broadcastError(String errorMessage) throws Exception;


     //Sends the final screen with winners and scores to ALL clients.

    void broadcastWinner(GameState finalState) throws Exception;

}
