package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.controller.ServerController;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public RmiServer(ServerController controller) throws RemoteException {
        super(); // crea lo Skeleton, mette in ascolto sulla rete
        this.controller = controller;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Map<String, VirtualViewRmi> copy;
            synchronized (this) {
                copy = new LinkedHashMap<>(clients);
            }
            // we ping every client
            for (Map.Entry<String, VirtualViewRmi> entry : copy.entrySet()) {
                try {
                    entry.getValue().ping();
                } catch (RemoteException e) {
                    // if it fails, client is disconnected
                    controller.handleDisconnection(entry.getKey());
                }
            }
        }, 5, 5, TimeUnit.SECONDS); // ping every 5 seconds
    }
    @Override
    public void connect(String nickname, VirtualViewRmi cur) throws RemoteException {
        controller.connect(nickname, cur);
    }

    @Override
    public void setExpectedPlayers(String nickname, int numPlayers) throws RemoteException {
        controller.setExpectedPlayers(nickname, numPlayers);
    }

    @Override
    public void moveTotem(String nickname, String offerTileId) throws RemoteException {
        controller.moveTotem(nickname, offerTileId);
    }

    @Override
    public void addCard(String nickname, String cardId) throws RemoteException {
        controller.addCard(nickname, cardId);
    }

    @Override
    public void addExtraCard(String nickname, String cardId) throws RemoteException {
        controller.addExtraCard(nickname, cardId);
    }

    @Override
    public void skipExtraDraw(String nickname) throws RemoteException {
        controller.skipExtraDraw(nickname);
    }
    //
    @Override
    public synchronized void registerClient(String nickname, Object cur) {
        try {
            NetworkMode node = (NetworkMode) cur;
            if (!node.isSocket()) {
                clients.put(nickname, (VirtualViewRmi) cur); //if it's  NOT socket, we put it in the RMI map
            }
        } catch (Exception e) {
            //ignore
        }
    }
    // registerClient and unregisterClient are synchronized to prevent data races on clients
    @Override
    public synchronized void unregisterClient(String nickname) {
        clients.remove(nickname);
    }

    @Override
    public void broadcastUpdate(GameState gameState) {
        List<VirtualViewRmi> currentClients;
        synchronized (this) {
            currentClients = new ArrayList<>(clients.values()); // to prevent concurrent modifications
        }
        for (VirtualViewRmi client : currentClients) {
            try {
                client.updateView(gameState);
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed to broadcast update", e);
            }
        }
    }

    @Override
    public void sendError(String nickname, String errorMessage) {
        VirtualViewRmi client = clients.get(nickname);
        if (client == null) {
            return;
        }

        try {
            client.signalError(errorMessage);
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed to send error to " + nickname, e);
        }
    }

    @Override
    public void broadcastError(String errorMessage) {
        List<VirtualViewRmi> currentClients;
        synchronized (this) {
            currentClients = new ArrayList<>(clients.values()); // to prevent concurrent modifications
        }
        for (VirtualViewRmi client : currentClients) {
            try {
                client.signalError(errorMessage);
            } catch (RemoteException e) {
                //ignore
            }
        }
    }

    @Override
    public void broadcastWinner(GameState finalState) {
        List<VirtualViewRmi> currentClients;
        synchronized (this) {
            currentClients = new ArrayList<>(clients.values()); // to prevent concurrent modifications
        }
        for (VirtualViewRmi client : currentClients) {
            try {
                client.showWinner(finalState);
            } catch (RemoteException e) {
                throw new IllegalStateException("Failed to broadcast winner", e);
            }
        }
    }
    @Override
    public synchronized void clearClients() {
        clients.clear();
    }




}
