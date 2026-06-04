package it.polimi.ingsw.am46;

import it.polimi.ingsw.am46.server.controller.ServerController;
import it.polimi.ingsw.am46.network.VirtualViewAdapter;
import it.polimi.ingsw.am46.network.rmi.server.RmiServer;
import it.polimi.ingsw.am46.network.socket.server.SocketServer;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ServerLauncher {
    /*
     Entry point for the RMI server. Creates the Registry (Yellow Pages), instantiates RmiServer,
     registers it under a name, and sets everything to listen.
     Separated from RmiServer to adhere to the single responsibility principle.
     */

    public static final String SERVER_NAME = "MesosServer";
    public static final int REGISTRY_PORT = 1099;
    public static final int SOCKET_PORT = 1234;

    public static void main(String[] args) throws Exception {
        String myIp;
        try (final DatagramSocket socket = new DatagramSocket()) {
            // Tentiamo una connessione fittizia per capire quale interfaccia di rete è attiva
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            myIp = socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            // Se non c'è internet o fallisce, ripieghiamo su localhost
            myIp = "127.0.0.1";
        }
        System.setProperty("java.rmi.server.hostname", myIp);
        System.out.println("===STARTING MESOS SERVER===");

        // Create the ServerController
        ServerController controller = new ServerController();
        controller.setResilienceEnabled(true);

        // Create the RmiServer, passing the controller
        // The superclass (UnicastRemoteObject) automatically exports it
        // and creates the skeleton
        RmiServer rmiServer = new RmiServer(controller);
        //controller.setVirtualView(rmiServer);

        // Create the RMI Registry on the configured port
        // The Registry acts as a discovery service for remote objects
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);



        // Bind the RmiServer instance into the Registry
        // Clients will retrieve it using registry.lookup(SERVER_NAME)
        registry.rebind(SERVER_NAME, rmiServer);

        // Print startup messages
        // The server now waits for incoming remote calls
        System.out.println("RMI Server started successfully!");
        System.out.println("Listening on port: " + REGISTRY_PORT);
        System.out.println("Registered service name: " + SERVER_NAME);

        // ---------------------------------------------------------
        // SETUP SOCKET
        // ---------------------------------------------------------
        System.out.println("[LOG] Inizializzazione Socket Server...");
        ServerSocket serverSocket = new ServerSocket(SOCKET_PORT);
        SocketServer socketServer = new SocketServer(serverSocket, controller);


        Thread socketThread = new Thread(socketServer, "socket-accept-thread");
        socketThread.start();
        System.out.println("[LOG] Socket Server pronto sulla porta: " + SOCKET_PORT);

        // ---------------------------------------------------------
        // SETUP VIRTUAL VIEW
        // ---------------------------------------------------------
        VirtualViewAdapter adapter = new VirtualViewAdapter(rmiServer, socketServer);
        controller.setVirtualView(adapter);

        System.out.println("\n=== MESOS SERVER IN ASCOLTO ===");
    }




}

