package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.network.VirtualServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServerRmi extends Remote, VirtualServer {
    /*
     * VirtualServer's RMI specialization.
     * Extends Remote → all methods must declare
     * throws RemoteException to be callable over the network.

     * With RMI, the cur parameter is of type VirtualViewRmi
     * (a remote object accessible over the network).
     */
}
