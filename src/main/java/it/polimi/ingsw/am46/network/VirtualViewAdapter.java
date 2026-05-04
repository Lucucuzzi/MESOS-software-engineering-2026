package it.polimi.ingsw.am46.network;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.rmi.server.RmiServer;
import it.polimi.ingsw.am46.network.socket.server.SocketServer;

// The Adapter that connects RMI and Socket.
// Receives updates from the ServerController and splits them sending them to both physical servers.
// Without this class is impossible to play with different technologies because an RMI client will not see update of a socket client

public class VirtualViewAdapter implements VirtualView {

    private final VirtualView socketServer;
    private final VirtualView rmiServer;

    public VirtualViewAdapter(VirtualView rmiServer, VirtualView socketServer) {
        this.socketServer = socketServer;
        this.rmiServer = rmiServer;
    }

    @Override
    public void registerClient(String nickname, NetworkMode cur){
        try {

            if (cur.isSocket()) {
                socketServer.registerClient(nickname, cur);
            } else {
                rmiServer.registerClient(nickname, cur);
            }
        } catch (Exception e) {
            System.err.println("Error during registration of client in the server: " + e.getMessage());
        }
    }
    @Override
    public void unregisterClient(String nickname){
        socketServer.unregisterClient(nickname);
        rmiServer.unregisterClient(nickname);
    }

    @Override
    public void broadcastUpdate(GameState gameState) throws Exception {
        socketServer.broadcastUpdate(gameState);
        rmiServer.broadcastUpdate(gameState);
    }

    @Override
    public void sendError(String nickname, String errorMessage) throws Exception {
        rmiServer.sendError(nickname, errorMessage);
        socketServer.sendError(nickname, errorMessage);
    }

    @Override
    public void broadcastError(String errorMessage) throws Exception {
        rmiServer.broadcastError(errorMessage);
        socketServer.broadcastError(errorMessage);
    }

    @Override
    public void broadcastWinner(GameState finalState) throws Exception {
        rmiServer.broadcastWinner(finalState);
        socketServer.broadcastWinner(finalState);
    }

    @Override
    public void clearClients() {
        rmiServer.clearClients();
        socketServer.clearClients();
    }

}
