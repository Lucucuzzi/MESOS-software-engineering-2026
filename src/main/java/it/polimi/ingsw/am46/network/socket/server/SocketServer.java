package it.polimi.ingsw.am46.network.socket.server;

import it.polimi.ingsw.am46.controller.ServerController;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.dto.GameState;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer implements VirtualView, Runnable {
    private final ServerSocket serverSocket;
    private final ServerController controller;

    private final Map<String, SocketClientHandler> clients = new LinkedHashMap<>();

    public SocketServer(ServerSocket serverSocket, ServerController controller) {
        this.serverSocket = serverSocket;
        this.controller = controller;
        // PING (Like RmiServer)
       startHeartbeatMonitor();
    }

    // Sockets don't throw exceptions on disconnected writes (unlike RMI). (Socket is async)
    // We use a manual timeout (15s) since the last "pong" to detect dropped connections.

    private void startHeartbeatMonitor() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Map<String, SocketClientHandler> copy;
            synchronized (this) {
                copy = new LinkedHashMap<>(clients);
            }

            long now = System.currentTimeMillis();
            long timeoutLimit = 15000; // 15 seconds tolerance

            for (Map.Entry<String, SocketClientHandler> entry : copy.entrySet()) {
                SocketClientHandler handler = entry.getValue();
                String nick = entry.getKey();

                // Check timeoutLimit
                if (now - handler.getLastPongTime() > timeoutLimit) {
                    System.err.println("Timeout for client: " + nick);
                    handler.stopRunning();
                    controller.handleDisconnection(nick);
                }
                // If it's still alive, we send ping
                else {
                    handler.sendPing();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }



    @Override
    public void run() {
        System.out.println("Socket server listening on port " + serverSocket.getLocalPort());
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Thread t = getThread(clientSocket, in);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Socket server stopped: " + e.getMessage());
        }
    }

    private Thread getThread(Socket clientSocket, BufferedReader in) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);

        SocketClientHandler handler = new SocketClientHandler(controller, in, out);
        // we don't know the nickname yet, it will arrive in the first connect()
        Thread t = new Thread(handler, "socket-handler-unknown");
        t.setDaemon(true); // Setting as daemon ensures the network listener terminates automatically when the user closes the main UI window.
        return t;
    }

    //==================================================================================================================
    // VIRTUAL VIEW - TO THE SOCKET CLIENT (CALLED BY MULTIPLEXER)
    //==================================================================================================================

    @Override
    public synchronized void registerClient(String nickname, NetworkMode cur) {
        try {

            // If it's a socket, put in the list
            if (cur.isSocket()) {
                clients.put(nickname, (SocketClientHandler) cur);
            }
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    public synchronized void unregisterClient(String nickname) {
        clients.remove(nickname);
    }

    @Override
    public synchronized void clearClients() {
        clients.clear();
    }

    @Override
    public void broadcastUpdate(GameState gameState) {
        List<SocketClientHandler> copy;
        synchronized (this) {
            copy = new ArrayList<>(clients.values());
        }
        for (SocketClientHandler client : copy) {
            client.sendUpdate(gameState);
        }
    }

    @Override
    public void sendError(String nickname, String errorMessage) {
        SocketClientHandler client;
        synchronized (this) {
            client = clients.get(nickname);
        }
        if (client != null) {
            client.sendError(errorMessage);
        }
    }

    @Override
    public void broadcastError(String errorMessage) {
        List<SocketClientHandler> copy;
        synchronized (this) {
            copy = new ArrayList<>(clients.values());
        }
        for (SocketClientHandler client : copy) {
            client.sendError(errorMessage);
        }
    }

    @Override
    public void broadcastWinner(GameState finalState) {
        List<SocketClientHandler> copy;
        synchronized (this) {
            copy = new ArrayList<>(clients.values());
        }
        for (SocketClientHandler client : copy) {
            client.sendWinner(finalState);
        }
    }



}
