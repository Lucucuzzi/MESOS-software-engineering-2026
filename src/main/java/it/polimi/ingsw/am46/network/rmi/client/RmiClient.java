package it.polimi.ingsw.am46.network.rmi.client;

import java.rmi.server.UnicastRemoteObject;
import it.polimi.ingsw.am46.network.GameState;
import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.View;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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

    // Il modello locale (cache lato client)
    private final LocalModel localModel;

    // Il controller lato client
    // Gestisce gli input dell'utente e chiama server.moveTotem ecc.
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

}
