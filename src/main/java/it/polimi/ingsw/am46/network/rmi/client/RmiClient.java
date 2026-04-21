package it.polimi.ingsw.am46.network.rmi.client;

import java.rmi.server.UnicastRemoteObject;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;

import java.rmi.NotBoundException;
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

public class RmiClient extends UnicastRemoteObject
        implements VirtualViewRmi{

    // Riferimento allo stub del server
    // Ottenuto via registry.lookup()
    private final VirtualServerRmi server;

    private final LocalModel localModel;

    // Il controller lato client
    private final ClientController clientController;

    public RmiClient(VirtualServerRmi server,
                     LocalModel localModel,
                     ClientController clientController)
            throws RemoteException {
        super(); // rende questo oggetto remoto raggiungibile
        this.server = server;
        this.localModel = localModel;
        this.clientController = clientController;
    }


    @Override
    public void updateView(GameState gameState)
            throws RemoteException {
        // The server sent an update
        // Update the LocalModel → notify the View
        // WARNING: this method is called in an RMI thread
        // separate from the CLI thread → potential data race
        // on the LocalModel is todo synchronized
    }

    @Override
    public void signalError(String errorMessage)
            throws RemoteException {
        // The server has reported an error for this client
        // E.g.: “It's not your turn!”, “Not enough food!”
    }
    @Override
    public void showWinner(GameState finalState)
            throws RemoteException {
        // The game is over — display the final screen with winners and scores
        // TODO: Notify the View that the game is over
    }


}
