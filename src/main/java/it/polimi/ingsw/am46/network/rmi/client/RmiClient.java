package it.polimi.ingsw.am46.network.rmi.client;

import java.rmi.server.UnicastRemoteObject;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.LocalModel;

import java.rmi.RemoteException;

/**
 * Concrete implementation of the RMI client.
 * Extends UnicastRemoteObject → makes this object remote,
 * accessible by the server over the network. This is the physical CUR
 * with RMI: the server calls updateView() on this object
 * as if it were local, but the JVM handles the transport.
 * Implements VirtualViewRmi → receives notifications from the server
 * (updateView, signalError, showWinner).
 */
public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi{

    private final LocalModel localModel;

    /**
     * Instantiates a new Rmi client.
     *
     * @param localModel the local model
     * @throws RemoteException the remote exception
     */
    public RmiClient(LocalModel localModel) throws RemoteException {
        super();
        this.localModel = localModel;
    }


    @Override
    public void updateView(GameState gameState)
            throws RemoteException {
        localModel.updateValue(gameState);
    }

    @Override
    public void signalError(String errorMessage)
            throws RemoteException {
        localModel.notifyError(errorMessage);
        // The server has reported an error for this client
    }

    @Override
    public void showWinner(GameState finalState)
            throws RemoteException {
        localModel.updateValue(finalState);
        // Notify the View that the game is over
    }

    @Override
    public void ping() throws RemoteException {
        // Server calls this method to check if player is online
    }
    @Override
    public boolean isSocket() throws RemoteException {
        return false;
    }

    @Override
    public void abortGame(String message) throws RemoteException {
        localModel.notifyAbort("Game aborted: " + message);
    }


}
