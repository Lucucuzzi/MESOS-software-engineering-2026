package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.server.controller.ServerController;
import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.network.async.AsyncBroadcastManager;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

/*
 * Concrete implementation of the RMI server.
 * Extends UnicastRemoteObject → automatically creates the skeleton,
 * sets the server to listen on the network, and makes the methods
 * remotely invocable.
 * Implements VirtualServerRmi → receives commands from clients.
 * Implements VirtualView → manages broadcasts to observers.
 * It serves as both the command entry point (VirtualServerRmi)
 * and the Broadcast Manager (VirtualView).
 */

/**
 * The type Rmi server.
 */
public class RmiServer extends UnicastRemoteObject
        implements VirtualServerRmi, VirtualView {

    // ServerController reference to call the game logic methods (connect, moveTotem, etc.)
    private final ServerController controller;

    // We replace the old Map<String, VirtualViewRmi> map with the Manager.
    // The manager is not just a container, but an active system that manages
    // the dispatch threads for every single registered client.
    private final AsyncBroadcastManager broadcastManager;

    // to verify that the RMI connection is still active "under the hood".
    private final ScheduledExecutorService pingScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                // Let's create a thread dedicated to ping and call it "rmi-ping"
                // to easily find it in the debugger if there are problems.
                Thread t = new Thread(r, "rmi-ping");
                // Daemon = true means that if the server shuts down, this thread
                // doesn't hang around blocking the computer.
                t.setDaemon(true);
                return t;
            });

    /**
     * Instantiates a new Rmi server.
     *
     * @param controller the controller
     * @throws RemoteException the remote exception
     */
    public RmiServer(ServerController controller) throws RemoteException {
        super();
        this.controller = controller;
        // Pass disconnection handling to the broadcast manager
        this.broadcastManager = new AsyncBroadcastManager(
                // We initialize the asynchronous manager.
                // Fundamental: we pass it the reference to the 'handleDisconnection'
                // method of the controller. This way, if the manager discovers that a client has died
                // while trying to send it an update, he can notify the game automatically.
                controller::handleDisconnection
        );
        // We start the periodic ping cycle towards the clients.
        startPing();
    }

    // =========================================================
    // VirtualServerRmi - Receiving commands from clients
    // =========================================================

    @Override
    public void connect(String nickname, String colorName, VirtualViewRmi cur)
            throws RemoteException, GameAlreadyStartedException, InvalidConnectionException {

        try {
            controller.connect(nickname, colorName, cur);
            // DO NOT call broadcastManager.registerClient() here:
            // controller.connect() → virtualView.registerClient() → broadcastManager.registerClient()
            // already does this. Double call = two delivery threads for the same client.

        } catch (NicknameOfflineException e) {
            // RMI: silent redirect. The client does not see exceptions,
            // it only receives the broadcastUpdate that arrives from reconnect().
            System.out.println("[RMI] Nickname '" + nickname + "' offline. Redirect to reconnect().");
            reconnect(nickname, cur);

        } catch (GameAlreadyStartedException | InvalidConnectionException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[RMI] Error in connect for" + nickname + ": " + e.getMessage());
            throw new InvalidConnectionException("Connection failed: " + e.getMessage());
        }
    }

    @Override
    public synchronized void reconnect(String nickname, VirtualViewRmi cur){
        // 1. Remove the old dead stub
        broadcastManager.unregisterClient(nickname);

        // 2. Register the new stub
        broadcastManager.registerClient(nickname, cur);

        // 3. Update model, clear timer, broadcastUpdate
        // controller.reconnect() NO longer calls virtualView.registerClient/unregisterClient
        controller.reconnect(nickname, cur);

        System.out.println("[RMI] Reconnect completated for " + nickname);
    }


    @Override
    public List<String> getAvailableColors() throws RemoteException, Exception {
        var availableEnums = controller.getAvailableColors();

        // 2. We transform them into strings to send them over the network in a "stupid" way
        List<String> stringColors = new ArrayList<>();
        for (var c : availableEnums) {
            stringColors.add(c.name());
        }
        return stringColors;
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


    // =========================================================
    // VirtualView — sends notifications to clients
    // =========================================================

    @Override
    public synchronized void registerClient(String nickname, NetworkMode cur) {
        // Instead of checking the class with instanceof, we ask the object
        // itself whether it represents a Socket or RMI connection.
        try {
            if (!cur.isSocket()) {
                // If it is not a socket, by exclusion in this project it is an RMI client.
                // We cast it to VirtualViewRmi to pass it to the manager.
                // The cast is safe because we just verified the nature of the network.
                VirtualViewRmi rmiView = (VirtualViewRmi) cur;
                broadcastManager.registerClient(nickname, rmiView);
            }
            // If cur.isSocket() is true, we do nothing:
            // this is the RMI server and should not manage Socket clients.
        } catch (RemoteException e) {
            // We handle any communication error during the check
            System.err.println("[RMI] Error while checking the network type for: " + nickname);
        }
    }


    // registerClient and unregisterClient are synchronized to prevent data races on clients
    @Override
    public synchronized void unregisterClient(String nickname) {
        broadcastManager.unregisterClient(nickname);
    }


    @Override
    public void broadcastUpdate(GameState gameState) {
        // NON-BLOCKING method: Don't physically send the data now, but "park" it
        // in the manager queues. Returns in microseconds, allowing the server
        // to immediately return to handling game logic without waiting for clients.
        broadcastManager.broadcastUpdate(gameState);
    }

    @Override
    public void sendError(String nickname, String errorMessage) {
        // Errors are critical: if a user makes an invalid move, they should know about it right away.
        // Let's get the client reference (the stub) directly from the manager.
        VirtualViewRmi view = broadcastManager.getView(nickname);

        if (view != null) {
            // Let's create a disposable thread just for this error.
            // Why? Because we don't want to clog up the game message queue (Broadcast)
            // with error messages, but we still want to avoid the server crashing
            // if the client network is slow right now.
            new Thread(() -> {
                try {
                    view.signalError(errorMessage);
                } catch (RemoteException e) {
                    // If the call fails, the client is probably crashed.
                    // We remove it from the manager and notify the controller to handle cleanup.
                    broadcastManager.unregisterClient(nickname);
                    controller.handleDisconnection(nickname);
                }
            }, "error-send-" + nickname).start();
        }
    }

    @Override
    public void broadcastError(String errorMessage) {
        // There is no longer any need to create a local list or use synchronized(this).
        // The broadcastManager internally manages the client list in a thread-safe manner.

        // We use a separate thread for error broadcast.
        // Why? Because errors (e.g. "The server is about to shut down") often have to
        // travel on a fast lane and not get queued behind
        // heavy GameState updates.
        new Thread(() -> {
            // We ask the manager to send the message to all registered RMI clients.
            // The manager will execute the calls in parallel or sequentially in its threads,
            // isolating any crashes of individual clients.
            broadcastManager.broadcastError(errorMessage);
        }, "broadcast-error-thread").start();
    }

    @Override
    public void broadcastWinner(GameState finalState) {
        // Winner notification — goes through the normal async queue
        broadcastManager.broadcastUpdate(finalState);
    }

    @Override
    public void clearClients() {
        // We ask the manager to clear everything
        broadcastManager.clearClients();
    }

    @Override
    public void broadcastAbort(String reason) {
        // Delegates asynchronous sending to all RMI clients to the manager.
        // The RMI server is immediately available for other operations.
        broadcastManager.broadcastAbort(reason);
    }

    private void startPing() {
        pingScheduler.scheduleAtFixedRate(() -> {
            Map<String, VirtualViewRmi> views = broadcastManager.getAllViews();

            for (Map.Entry<String, VirtualViewRmi> entry : views.entrySet()) {
                String nickname = entry.getKey();
                VirtualViewRmi view = entry.getValue();

                try {
                    view.ping();
                } catch (RemoteException e) {
                    broadcastManager.unregisterClient(nickname);
                    controller.handleDisconnection(nickname);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        broadcastManager.shutdown();
        pingScheduler.shutdownNow();
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int numPlayers) throws RemoteException {
        return controller.getLeaderboard(numPlayers);
    }

    @Override
    public int getPlayerPosition(String nickname, int numPlayers) throws RemoteException {
        return controller.getPlayerPosition(nickname, numPlayers);
    }

}
