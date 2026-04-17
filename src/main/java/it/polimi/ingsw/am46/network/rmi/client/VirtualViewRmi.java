package it.polimi.ingsw.am46.network.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualViewRmi extends Remote {
    /*
     * RMI implementation of ClientUpdateReceiver.
     * Extends Remote → the server can call these methods
     * on the client over the network as if they were local.
     * Implemented by RmiClient, which extends UnicastRemoteObject.
     * This is what makes the client “reachable” by the server.
     */
}
