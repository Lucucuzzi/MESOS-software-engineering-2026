package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.controller.ServerController;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RmiLauncher {
    /*
     Entry point for the RMI server. Creates the Registry (Yellow Pages), instantiates RmiServer,
     registers it under a name, and sets everything to listen.
     Separated from RmiServer to adhere to the single responsibility principle.
     */

    //public static final String SERVER_NAME = "MesosServer";
    //public static final int REGISTRY_PORT = Numero della porta che vogliamo (?);

    public static void main(String[] args) throws Exception {

        // Create the ServerController (contains the Game logic)
        // This object will validate commands and notify the VirtualView

        // Create the RmiServer, passing the controller
        // The superclass (UnicastRemoteObject) automatically exports it
        // and creates the skeleton

        // Create the RMI Registry on the configured port
        // The Registry acts as a discovery service for remote objects

        // Bind the RmiServer instance into the Registry
        // Clients will retrieve it using registry.lookup(SERVER_NAME)

        // Print startup messages
        // The server now waits for incoming remote calls
    }
}
