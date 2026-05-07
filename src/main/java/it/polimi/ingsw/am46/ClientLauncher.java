package it.polimi.ingsw.am46;

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

public class ClientLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BENVENUTO IN MESOS ===");

        System.out.println("Scegli la connessione: [1] RMI  [2] Socket");
        int networkChoice = Integer.parseInt(scanner.nextLine());

        System.out.println("Inserisci l'IP del server (es. localhost):");
        String serverIp = scanner.nextLine();

        System.out.println("Scegli l'interfaccia: [1] TUI (Testo)  [2] GUI (Grafica)");
        int uiChoice = Integer.parseInt(scanner.nextLine());

        try {
            LocalModel localModel = new LocalModel();
            ClientController controller = new ClientController(localModel);

            // ----------------------------------------------------------------------
            // FIX: Creiamo la View e registriamo l'Observer PRIMA della connect.
            // L'oggetto View esiste ed "ascolta", ma NON ruba l'input perché
            // non abbiamo ancora chiamato view.start()!
            // ----------------------------------------------------------------------
            CountDownLatch latch = new CountDownLatch(1);
            ViewFactory.ViewType type = (uiChoice == 1) ? ViewFactory.ViewType.CLI : ViewFactory.ViewType.GUI;
            GameView view = ViewFactory.create(type, localModel, controller, latch);
            localModel.registerObserver(view);
            // ----------------------------------------------------------------------

            // 1. SETUP RETE E LOGIN
            if (networkChoice == 1) {
                System.out.println("Connessione al server RMI in corso...");
                Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
                VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");
                controller.setServer(serverStub);

                // RECUPERO COLORI (RMI)
                List<String> liberi = serverStub.getAvailableColors();

                System.out.println("Inserisci il tuo Nickname:");
                String nicknameUtente = scanner.nextLine();

                System.out.println("Colori disponibili: " + liberi.toString());
                System.out.print("Scegli il tuo colore: ");
                String colorInput = scanner.nextLine().trim();

                controller.setNickname(nicknameUtente);
                RmiClient rmiClient = new RmiClient(localModel);

                serverStub.connect(nicknameUtente, colorInput, rmiClient);
                System.out.println("Connesso con successo via RMI!");

            } else if (networkChoice == 2) {
                System.out.println("[LOG] Connessione Socket a " + serverIp + ":" + SOCKET_PORT + "...");
                Socket socket = new Socket(serverIp, SOCKET_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                System.out.println("[LOG] Socket TCP aperto con successo.");

                // Passiamo anche l'InputStream (in) al Proxy!
                SocketClientProxy serverProxy = new SocketClientProxy(out, in);
                controller.setServer(serverProxy);

                // RECUPERO COLORI (Socket Sincrono)
                List<String> liberi = serverProxy.getAvailableColors();

                System.out.println("Inserisci il tuo Nickname:");
                String nicknameUtente = scanner.nextLine();

                System.out.println("Colori disponibili: " + liberi.toString());
                System.out.print("Scegli il tuo colore: ");
                String colorInput = scanner.nextLine().trim();

                controller.setNickname(nicknameUtente);

                System.out.println("[LOG] Invio richiesta di connect() al server...");
                serverProxy.connect(nicknameUtente, colorInput, null);
                System.out.println("Connesso con successo via Socket!");

                SocketListener listener = new SocketListener(in, localModel, serverProxy);
                Thread listenerThread = new Thread(listener, "socket-listener-thread");
                listenerThread.setDaemon(true);
                listenerThread.start();
                System.out.println("[LOG] SocketListener in background avviato.");

            } else {
                System.out.println("Scelta non valida! Chiusura.");
                return;
            }

            view.start();

            // WAIT FOR MAIN THREAD
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Client interrotto bruscamente.");
            }

            // GraceFull Showtdown
            System.out.println("\n[LOG] Chiusura del client...");
            view.stop();
            System.exit(0);

        } catch (Exception e) {
            System.err.println("\nERRORE FATALE: " + e.getMessage());
            System.err.println("La connessione è stata rifiutata. Riavvia il client e riprova.");
            System.exit(0);
        }
    }
}