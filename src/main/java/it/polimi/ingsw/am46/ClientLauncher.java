package it.polimi.ingsw.am46;

import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.network.rmi.client.RmiClient;
import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.network.socket.client.SocketClientProxy;
import it.polimi.ingsw.am46.network.socket.client.SocketListener;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.GameView;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.ViewFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static it.polimi.ingsw.am46.ServerLauncher.SOCKET_PORT;

/**
 * The type Client launcher.
 */
public class ClientLauncher {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== WELCOME TO MESOS ===");

        System.out.println("Choose connection: [1] RMI  [2] Socket");
        int networkChoice = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter the server IP (e.g., localhost):");
        String serverIp = scanner.nextLine();

        System.out.println("Choose the interface: [1] TUI (Text)  [2] GUI (Graphical)");
        int uiChoice = Integer.parseInt(scanner.nextLine());

        try {
            LocalModel localModel = new LocalModel();
            ClientController controller = new ClientController(localModel);

            // ----------------------------------------------------------------------
            // Create the View and register the Observer BEFORE the connect.
            // The View object exists and "listens", but it DOES NOT steal the input because
            // we haven't called view.start() yet!
            // ----------------------------------------------------------------------
            // Create a CountDownLatch initialized to 1 to keep the main thread alive.
            // Since the views (CLI/GUI) run on their own separate threads, without this latch
            // the main method would reach the end and terminate the application immediately.
            // The View will call latch.countDown() when the user decides to quit the game.
            CountDownLatch latch = new CountDownLatch(1);
            ViewFactory.ViewType type = (uiChoice == 1) ? ViewFactory.ViewType.CLI : ViewFactory.ViewType.GUI;
            GameView view = ViewFactory.create(type, localModel, controller, latch);
            localModel.registerObserver(view);
            // ----------------------------------------------------------------------

            // Variables to keep the connection open outside the loops
            VirtualServerRmi serverStub = null;
            SocketClientProxy serverProxy = null;
            BufferedReader socketIn = null;
            String nicknameUtente = "";
            String colorInput = null;

            // 1. NETWORK SETUP (We create the "pipes" but don't log in yet)
            if (networkChoice == 1) {
                System.out.println("Connecting to the RMI server...");
                Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
                serverStub = (VirtualServerRmi) registry.lookup("MesosServer");
                controller.setServer(serverStub);
            } else if (networkChoice == 2) {
                System.out.println("[LOG] Socket connection to " + serverIp + ":" + SOCKET_PORT + "...");
                Socket socket = new Socket(serverIp, SOCKET_PORT);
                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                System.out.println("[LOG] TCP Socket successfully opened.");

                // We also pass the InputStream (in) to the Proxy!
                serverProxy = new SocketClientProxy(out, socketIn);
                controller.setServer(serverProxy);
            } else {
                System.out.println("Invalid choice! Closing.");
                return;
            }

            // 2. LOGIN LOOP (Repeats in case of nickname or color error)
            boolean isConnected = false;
            boolean isReconnecting = false;


            while (!isConnected) {
                try {
                    if (!isReconnecting){
                        // RETRIEVING COLORS
                        List<String> liberi = (networkChoice == 1) ? serverStub.getAvailableColors() : serverProxy.getAvailableColors();

                        System.out.println("\nEnter your Nickname:");
                        nicknameUtente = scanner.nextLine();

                        if (nicknameUtente.isEmpty()) {
                            System.out.println("Nickname cannot be empty!");
                            continue;
                        }

                        System.out.println("Available colors: " + liberi);
                        System.out.print("Choose your color: ");
                        colorInput = scanner.nextLine().trim();

                        if (colorInput.isEmpty()) {
                            System.out.println("Color cannot be empty!");
                            continue;
                        }

                    }
                    controller.setNickname(nicknameUtente);
                    localModel.setMyNickname(nicknameUtente);

                    if (networkChoice == 1) {
                        RmiClient rmiClient = new RmiClient(localModel);
                        controller.setMyNetworkReference(rmiClient);
                        if (isReconnecting) {
                            // ✅ RECONNECTION: 2 parameters (nickname + RMI stub)
                            System.out.println("♻️  Attempting RMI reconnect...");
                            serverStub.reconnect(nicknameUtente, rmiClient);
                            System.out.println("✓ Reconnected via RMI!");

                        } else {
                            // ✅ NEW CONNECTION: 3 parameters (nickname + color + RMI stub)
                            System.out.println("Attempting RMI connect...");
                            serverStub.connect(nicknameUtente, colorInput, rmiClient);
                            System.out.println("Connected via RMI!");
                        }
                    } else {
                        // === SOCKET FLOW ===
                        controller.setMyNetworkReference(serverProxy);
                        if (isReconnecting) {
                            // SOCKET RECONNECTION: 1 parameter (nickname only)
                            System.out.println("Attempting Socket reconnect...");
                            serverProxy.reconnect(nicknameUtente,null);
                            // Start the listener BEFORE waiting for confirmation
                            SocketListener listener = new SocketListener(socketIn, localModel, serverProxy);
                            Thread listenerThread = new Thread(listener, "socket-listener-thread");
                            listenerThread.setDaemon(true);
                            listenerThread.start();
                            // Wait for explicit confirmation from the server (max 5 seconds)
                            long start = System.currentTimeMillis();
                            while (!localModel.isReconnectConfirmed()
                                    && System.currentTimeMillis() - start < 5000) {
                                Thread.sleep(100);
                            }

                        } else {
                            // NEW SOCKET CONNECTION: 3 parameters (third is null)
                            System.out.println("[LOG] Sending connect() request to the server...");
                            serverProxy.connect(nicknameUtente, colorInput, null);
                            SocketListener listener = new SocketListener(socketIn, localModel, serverProxy);
                            Thread listenerThread = new Thread(listener, "socket-listener-thread");
                            listenerThread.setDaemon(true);
                            listenerThread.start();
                            System.out.println("[LOG] Background SocketListener started.");
                        }
                        System.out.println("Connected via Socket!");

                    }
                    isConnected = true; // If we are here, no errors from the server!
                } catch (GameAlreadyStartedException e) {
                    System.out.println("\n❌ " + e.getMessage());
                    System.exit(0); // Stop the client
                } catch (NicknameOfflineException e) {
                    // This fires for Socket (SocketClientProxy throws exception)
                    // and never for RMI (RmiServer does silent redirect)
                    System.out.println("\n♻️  Player '" + nicknameUtente + "' found in current game!");
                    System.out.println("Automatically reconnecting...");
                    isReconnecting = true;
                    colorInput = null;
                    // The loop starts again with isReconnecting = true, skips the color request
                    // and calls reconnect() instead of connect()
                } catch (InvalidConnectionException e) {
                    System.out.println("\n❌ Connection error: " + e.getMessage());
                } catch (Exception e) {// Generic network error management
                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("offline") || msg.contains("reconnect"))) {
                        // SPECIAL CASE: The server indicates that the nickname is offline
                        System.out.println("\n♻️  Player '" + nicknameUtente + "' found in an ongoing game!");
                        System.out.println("Automatically reconnecting...");
                        isReconnecting = true;
                        colorInput = null; // No color needed to reconnect
                        continue; // Retry the loop with the reconnect flow
                    } else {
                        System.out.println("\nUnexpected error: " + e.getMessage());
                        e.printStackTrace();
                        isReconnecting = false;
                        colorInput = null;
                    }
                }
            }

            System.out.println("⏳ Synchronizing with the board...");
            while (localModel.getCurrentState() == null) {
                Thread.sleep(100);
            }

// isReconnecting is already correct for both protocols:
// - Socket: NicknameOfflineException caught in the loop → isReconnecting = true
// - RMI: NicknameOfflineException caught in the loop → isReconnecting = true
// or silent redirect → isReconnecting = false, but the GUI
// will show the scoreboard directly because currentPhaseName != "Lobby"
            boolean actuallyReconnecting = isReconnecting;

            if (actuallyReconnecting) {
                controller.setReconnecting(true);
                System.out.println(" Resuming game in progress...");
            }

            if (!actuallyReconnecting
                    && nicknameUtente.equals(localModel.getCurrentState().getHostNickname())  && !localModel.getCurrentState().isGameStarted()) {
                boolean validNum = false;
                while (!validNum) {
                    System.out.println("\nYOU ARE THE GAME HOST!");
                    System.out.println("Enter the number of expected players (from 2 to 5):");
                    try {
                        int num = Integer.parseInt(scanner.nextLine());
                        if (num < 2 || num > 5) {
                            System.out.println("Invalid number. Must be between 2 and 5.");
                            continue;
                        }
                        if (networkChoice == 1) {
                            serverStub.setExpectedPlayers(nicknameUtente, num);
                        } else {
                            serverProxy.setExpectedPlayers(nicknameUtente, num);
                        }
                        System.out.println("Number of players set. Waiting for others...");
                        validNum = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Enter a valid integer!");
                    } catch (Exception e) {
                        System.out.println("Communication error with the server: " + e.getMessage());
                    }
                }
            } else if (!actuallyReconnecting  && !localModel.getCurrentState().isGameStarted()) {
                System.out.println("\n⏳ You joined as a guest. Waiting for the Host or other players...");
            }



            // 4. VIEW START (Now the GUI will take control of the main thread)
            view.start();

            // WAIT FOR MAIN THREAD
            // Instead of scanner.nextLine(), tell the main thread to sleep until
            // something (e.g., the View when it receives "quit") calls latch.countDown().
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Client abruptly interrupted.");
            }
            //in catch if someone will disconnect with ctrl+c

            // GraceFull Showtdown
            System.out.println("\n[LOG] Closing the client...");
            view.stop();
            System.exit(0);

        } catch (Exception e) {
            System.err.println("\nFATAL ERROR: " + e.getMessage());
            System.err.println("The connection was refused. Restart the client and try again.");
            System.exit(0);
        }
    }
}