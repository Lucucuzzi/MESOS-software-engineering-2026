package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.network.VirtualServer;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServerRmi extends Remote, VirtualServer<VirtualViewRmi> {
    /*
     VirtualServer's RMI specialization.
     Extends Remote → all methods must declare throws RemoteException to be callable over the network.

     With RMI, the cur parameter is of type VirtualViewRmi
     (a remote object accessible over the network).
     */


    //Methods that can be called by clients via RMI (RemoteException required)
    void connect(String nickname, VirtualViewRmi cur) throws RemoteException;
    void setExpectedPlayers(String nickname, int numPlayers) throws RemoteException;

    void moveTotem(String nickname, String offerTileId) throws RemoteException;

    void addCard(String nickname, String cardId) throws RemoteException;

    void addExtraCard(String nickname, String cardId) throws RemoteException;

    void skipExtraDraw(String nickname) throws RemoteException;

}
