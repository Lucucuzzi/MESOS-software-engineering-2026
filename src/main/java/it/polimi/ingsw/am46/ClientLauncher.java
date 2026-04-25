package it.polimi.ingsw.am46;

import it.polimi.ingsw.am46.network.rmi.client.RmiClient;
import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.network.socket.client.SocketClientProxy;
import it.polimi.ingsw.am46.network.socket.client.SocketListener;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import static it.polimi.ingsw.am46.ServerLauncher.SOCKET_PORT;

public class ClientLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BENVENUTO IN MESOS ===");

        // 1. SCELTA RETE
        System.out.println("Scegli la connessione: [1] RMI  [2] Socket");
        int networkChoice = Integer.parseInt(scanner.nextLine());

        System.out.println("Inserisci l'IP del server (es. localhost):");
        String serverIp = scanner.nextLine();

        System.out.println("Inserisci il tuo Nickname:");
        String nicknameUtente = scanner.nextLine();

        // 2. SCELTA VIEW (Prepariamo il terreno)
        System.out.println("Scegli l'interfaccia: [1] TUI (Testo)  [2] GUI (Grafica)");
        int uiChoice = Integer.parseInt(scanner.nextLine());

        try {
            // Creiamo il LocalModel (che è indipendente dalla View)
            LocalModel localModel = new LocalModel();
            ClientController controller = new ClientController(localModel);
            controller.setNickname(nicknameUtente);

            // SETUP RETE
            if (networkChoice == 1) {
                System.out.println("Connessione al server RMI in corso...");
                Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
                VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");
                controller.setServer(serverStub);

                RmiClient rmiClient = new RmiClient(localModel);
                serverStub.connect(nicknameUtente, rmiClient);
                System.out.println("Connesso con successo via RMI!");
            } else if (networkChoice == 2) {
                System.out.println("[LOG] Connessione Socket a " + serverIp + ":" + SOCKET_PORT + "...");

                // 1. Apriamo il tubo TCP
                Socket socket = new Socket(serverIp, SOCKET_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                System.out.println("[LOG] Socket TCP aperto con successo.");

                // 2. Creiamo il Proxy (Il falso server a cui parlerà il ClientController)
                SocketClientProxy serverProxy = new SocketClientProxy(out);
                controller.setServer(serverProxy);

                // 3. Creiamo il Postino in background per ascoltare i pacchetti in arrivo
                SocketListener listener = new SocketListener(in, localModel, serverProxy);
                Thread listenerThread = new Thread(listener, "socket-listener-thread");
                listenerThread.setDaemon(true); // Il thread muore da solo se chiudi l'app
                listenerThread.start();
                System.out.println("[LOG] SocketListener in ascolto avviato.");

                // 4. Iniziamo il gioco mandando la richiesta di registrazione
                System.out.println("[LOG] Invio richiesta di connect() al server...");
                serverProxy.connect(nicknameUtente, null); // cur è null perché i Socket non passano oggetti!
                System.out.println("[LOG] Richiesta inviata. Connesso via Socket!");
            } else {
                System.out.println("Scelta non valida! Chiusura.");
                return;
            }

            // SETUP VIEW
            if (uiChoice == 1) {
                System.out.println("Avvio della TUI...");
                // TODO: TuiView tui = new TuiView(localModel, controller);
                // TODO: tui.start();
            } else {
                System.out.println("Avvio della GUI...");
                // TODO: Application.launch(GuiView.class, args);
            }

            // --- AGGIUNGO QUESTO BLOCCO PER IL TEST ---
            System.out.println("\n[LOG] Gioco in esecuzione in background.");
            System.out.println("[LOG] Premi INVIO qui sul client per chiudere il gioco e disconnetterti...");
            scanner.nextLine(); // Blocca il main finché non premi invio!
            System.out.println("[LOG] Chiusura client...");
            System.exit(0);
            // ------------------------------------------

        } catch (Exception e) {
            System.err.println("Errore fatale: " + e.getMessage());
        }
    }
}
