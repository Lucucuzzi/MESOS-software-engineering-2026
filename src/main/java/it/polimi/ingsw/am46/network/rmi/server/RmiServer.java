package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.controller.ServerController;
import it.polimi.ingsw.am46.network.GameState;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of the RMI server.
 * Extends UnicastRemoteObject → automatically creates the skeleton,
 * sets the server to listen on the network, and makes the methods
 * remotely invocable.
 * Implements VirtualServerRmi → receives commands from clients.
 * Implements VirtualView → manages broadcasts to observers.
 * It serves as both the command entry point (VirtualServerRmi)
 * and the Broadcast Manager (VirtualView).
 */

public class RmiServer extends UnicastRemoteObject
        implements VirtualServerRmi, VirtualView {

    // ServerController reference to call the game logic methods (connect, moveTotem, etc.)
    private final ServerController controller;

    // Map of nicknames to client CURs (VirtualViewRmi)
    // synchronized to prevent data races during connection/disconnection
    private final Map<String, VirtualViewRmi> clients
            = new LinkedHashMap<>();

    public RmiServer(ServerController controller)
            throws RemoteException {
        super(); // crea lo Skeleton, mette in ascolto sulla rete
        this.controller = controller;
    }
    @Override
    public void connect(String nickname, VirtualViewRmi cur) throws RemoteException {
        // Register the client endpoint and delegate to controller
    }

    @Override
    public void moveTotem(String nickname, String offerTileId) throws RemoteException {
        // Forward the command to the controller
    }

    @Override
    public void addCard(String nickname, String cardId) throws RemoteException {
        // Forward the command to the controller
    }

    @Override
    public void addExtraCard(String nickname, String cardId) throws RemoteException {
        // Forward the command (null = skip)
    }

    @Override
    public void skipExtraDraw(String nickname) throws RemoteException {
        // Shortcut for addExtraCard(null)
    }

    @Override
    public void registerClient(String nickname, Object cur) {
        // Add client endpoint to the map
    }

    @Override
    public void unregisterClient(String nickname) {
        // Remove client endpoint from the map
    }

    @Override
    public void broadcastUpdate(GameState gameState) {
        // Send updateView() to all clients
    }

    @Override
    public void sendError(String nickname, String errorMessage) {
        // Send signalError() only to the target client
    }

    @Override
    public void broadcastWinner(GameState finalState) {
        // Send showWinner() to all clients
    }




}
