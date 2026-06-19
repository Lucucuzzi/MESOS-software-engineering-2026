package it.polimi.ingsw.am46.network.async;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/*
 Async broadcast manager for RMI clients.

 Each registered client gets its own bounded queue (capacity=2) and its own dedicated delivery thread.

 Why bounded queue with capacity 2?
 The server can send multiple GameStates very quickly during automatic
 phase transitions (ResolveEvents → EndRound → PlaceTotem fires 3 updates
 in milliseconds). A slow client shouldn't accumulate all of them —
 only the latest state matters. When the queue is full, old states
 are evicted and replaced with the newest one.

 Why one thread per client?
 If a single dispatch thread serves all clients sequentially, one slow
 client blocks delivery to everyone. With one thread per client,
 Carlo's 3-second lag affects only Carlo.
 */

// NOTE: This manager is used solely to handle the synchronous nature of RMI.
// Unlike sockets, where sending is handled by the operating system’s TCP buffers,
// in RMI, every call to `broadcastUpdate()` would block the server until the client responds.
// If an RMI client experiences lag, it would halt the game for everyone. This class isolates each
// RMI client in a dedicated thread (the deliveryLoop), ensuring that delays experienced by a
// single player do not affect the others or the rest of the server in any way.

/**
 * The type Async broadcast manager.
 */
public class AsyncBroadcastManager {
    // We’re using a very small capacity (2). Why?
    // In an online game, if a client is slow, there’s no point in sending it 10 old states.
    // We’ll only send it the latest one available to keep it updated in real time.
    private static final int QUEUE_CAPACITY = 2;

    // ConcurrentHashMap is crucial: it allows multiple threads to add/remove
    // clients at the same time without crashing the server.
    private final Map<String, ClientChannel> channels
            = new ConcurrentHashMap<>();


    /* This component is essential for ensuring that the server is robust and does not waste resources.
    Without this DisconnectionHandler,
    the server would continue to try to communicate with ‘ghosts’ (clients that no longer exist).
     */
    private final DisconnectionHandler onDisconnected;

    /**
     * The interface Disconnection handler.
     */
    @FunctionalInterface
    public interface DisconnectionHandler {
        /**
         * Handle.
         *
         * @param nickname the nickname
         */
        void handle(String nickname);
    }

    /**
     * Instantiates a new Async broadcast manager.
     *
     * @param onDisconnected the on disconnected
     */
    public AsyncBroadcastManager(DisconnectionHandler onDisconnected) {
        this.onDisconnected = onDisconnected;
    }


    /**
     * Register client.
     *
     * @param nickname the nickname
     * @param view     the view
     */
//Registers a new RMI client and starts its delivery thread.
     //Called by RmiServer when a client connects.
    public void registerClient(String nickname, VirtualViewRmi view) {
        // Each client has its own queue. If your network is slow,
        // only YOUR queue fills up, not that of others.
        /* We use LinkedBlockingQueue for three basic reasons:
           1. THREAD-SAFETY: The ServerController writes to the queue while the postman thread
           reads. This structure manages conflicts internally.
           2. EFFICIENCY (Blocking): If the queue is empty, the thread pauses
           automatically (does not consume CPU) until a new update arrives.
           3. LAG MANAGEMENT: Having a fixed capacity, if a client is too slow
           we can discard old data and keep only recent data.
         */
        LinkedBlockingQueue<GameState> queue
                = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // Let's create a dedicated thread (the "postman") for this specific client.
        // This way, if its network slows down, it will only block this thread
        // and not the entire server or other players.
        Thread thread = new Thread(
                () -> deliveryLoop(nickname, view, queue),
                "rmi-delivery-" + nickname
        );

        // Set as Daemon: If the server shuts down, these service threads
        // should not prevent the process from shutting down
        thread.setDaemon(true);
        thread.start();

        // We save the complete "channel" in the map to be able to retrieve it
        // when we need to do a broadcast or manage a disconnection.
        channels.put(nickname, new ClientChannel(queue, thread, view));
    }


     //Removes a client and interrupts its delivery thread.

    /**
     * Unregister client.
     *
     * @param nickname the nickname
     */
    public void unregisterClient(String nickname) {
        ClientChannel ch = channels.remove(nickname);
        if (ch != null) {
            // Stop the postman thread immediately.
            ch.thread.interrupt();
        }
    }

    /**
     * Broadcast update.
     *
     * @param state the state
     */
// Sends the new game state to all connected players.
    // The operation is instantaneous because it does not "talk" to the network, but only writes to memory locale.
    public void broadcastUpdate(GameState state) {
        // We loop through all the active channels in our ConcurrentHashMap map.
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            // For each client, we put the status in its personal queue.
            enqueue(entry.getValue().queue, state);
        }
    }

    /**
     * Send to one.
     *
     * @param nickname the nickname
     * @param state    the state
     */
// Sends a targeted update to a single player (e.g. for a reconnection or a private error).
    public void sendToOne(String nickname, GameState state) {
        // We get the "package" (queue + thread) associated with the nickname.
        ClientChannel ch = channels.get(nickname);
        if (ch != null) {
            enqueue(ch.queue, state);
        }
    }

    /**
     * Broadcast error.
     *
     * @param errorMessage the error message
     */
    public void broadcastError(String errorMessage) {
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            String nickname = entry.getKey();
            ClientChannel ch = entry.getValue();

            // We do NOT use ch.queue.offer because the queue only accepts GameState.
            // Let's create a quick "disposable" thread to send the error right away.
            new Thread(() -> {
                try {
                    ch.view.signalError(errorMessage);
                } catch (RemoteException e) {
                    // If it fails, we clean up the connection
                    unregisterClient(nickname);
                    onDisconnected.handle(nickname);
                }
            }, "error-sender-" + nickname).start();
        }
    }

    /**
     * Broadcast abort.
     *
     * @param reason the reason
     */
    public void broadcastAbort(String reason) {
        // Cicliamo su tutti i canali connessi
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            String nickname = entry.getKey();
            ClientChannel ch = entry.getValue();

            // We create a dedicated thread for each client.
            // We don't use the queue because the abort needs to bypass pending GameStates.
            new Thread(() -> {
                try {
                    ch.view.abortGame(reason);
                } catch (RemoteException e) {
                    // If the client doesn't respond, we formally disconnect it
                    unregisterClient(nickname);
                    onDisconnected.handle(nickname);
                }
            }, "abort-sender-" + nickname).start();
        }
    }

    /**
     * Clear clients.
     */
    public void clearClients() {
        // 1. We stop all dispatch threads (processLoop) for each client
        for (ClientChannel ch : channels.values()) {
            ch.thread.interrupt();
        }

        // 2. Let's clear the channel map
        channels.clear();

        System.out.println("[Manager] All clients have been removed and threads closed.");
    }

    /**
     * Gets view.
     *
     * @param nickname the nickname
     * @return the view
     */
// Returns the RMI stub (the "remote view") of a specific player.
    // This is used by the server to send direct communications, such as error messages
    // or "game started" signals that don't necessarily go through the broadcast queue.
    public VirtualViewRmi getView(String nickname) {
        ClientChannel ch = channels.get(nickname);
        return ch != null ? ch.view : null;
    }


    /**
     * Gets client count.
     *
     * @return the client count
     */
// It is useful for the server to know whether the lobby is full or whether there are enough players.
    public int getClientCount() {
        return channels.size();
    }

    /**
     * Shutdown.
     */
// Shuts down the entire asynchronous broadcast system.
    // Called when the server is shut down to avoid leaving "orphaned" threads.
    public void shutdown() {
        // Loop through all currently connected client channels.
        for (ClientChannel ch : channels.values()) {
            // We send an interrupt signal to each "postman" thread.
            // This will cause the threads to exit their deliveryLoop cleanly.
            ch.thread.interrupt();
        }
        // Let's empty the map: at this point the server no longer has registered clients.
        channels.clear();
    }



    private void enqueue(LinkedBlockingQueue<GameState> queue, GameState state) {
        // Let's try to insert the state. If the queue is full (offer returns false)...
        if (!queue.offer(state)) {
            // ...remove the oldest state present (poll) to make room...
            queue.poll();
            // ...and insert the most recent one. This ensures that the client
            // always receives the latest table situation and not stale data.
            queue.offer(state);
        }
    }

    /**
     * Gets all views.
     *
     * @return the all views
     */
    public Map<String, VirtualViewRmi> getAllViews() {
        Map<String, VirtualViewRmi> result = new HashMap<>();
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            result.put(entry.getKey(), entry.getValue().view());
        }
        return result;
    }



    // This method is the work done by each client's "postman" thread.
    private void deliveryLoop(String nickname,
                              VirtualViewRmi view,
                              LinkedBlockingQueue<GameState> queue) {
        try {
            // The thread continues to run until explicitly stopped.
            while (!Thread.currentThread().isInterrupted()) {
                // The thread pauses here (without consuming CPU) until something arrives
                // in the queue. As soon as a state arrives, "take" picks it up.
                GameState state = queue.take();
                try {
                    // Real RMI call to the client. This is where the potential delay occurs.
                    // If the network is slow, only this specific thread will be left waiting.
                    view.updateView(state);
                } catch (RemoteException e) {
                    // If the call fails, it means the client is crashed or offline.
                    System.err.println("[Broadcast] "
                            + nickname + " disconnected: " + e.getMessage());
                    // We notify the ServerController of the disconnection to clean up the game.
                    onDisconnected.handle(nickname);
                    // Let's exit the method: this will permanently terminate the postman thread.
                    return;
                }
            }
        } catch (InterruptedException e) {
            // If the thread is killed while waiting on the queue, we exit clean.
            Thread.currentThread().interrupt();
        }
    }

    // Immutable data structure that groups together everything needed to manage a client.
    // Let's use a record for clarity: it contains the message queue,
    // the thread that sends them and the RMI stub (view) to contact the client.
    private record ClientChannel(
            LinkedBlockingQueue<GameState> queue,
            Thread thread,
            VirtualViewRmi view
    ) {}
}
