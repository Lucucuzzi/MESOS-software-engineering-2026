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

    // ServerController — la logica di controllo e validazione
    private final ServerController controller;

    // Mappa nickname → CUR del client (VirtualViewRmi)
    // synchronized per evitare data races durante connect/disconnect
    private final Map<String, VirtualViewRmi> clients
            = new LinkedHashMap<>();

    public RmiServer(ServerController controller)
            throws RemoteException {
        super(); // crea lo Skeleton, mette in ascolto sulla rete
        this.controller = controller;
    }
}
