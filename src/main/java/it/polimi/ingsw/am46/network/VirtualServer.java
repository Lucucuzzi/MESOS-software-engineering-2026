package it.polimi.ingsw.am46.network;

public interface VirtualServer {
    /*
     * Base interface that defines the commands the client
     * can send to the server. Independent of the network technology.
     * With RMI → VirtualServerRmi extends Remote, VirtualServer
     * With Socket → VirtualServerSocket extends VirtualServer
     */
}
