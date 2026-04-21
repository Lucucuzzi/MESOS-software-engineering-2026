package it.polimi.ingsw.am46.network.rmi.client;

import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BENVENUTO IN MESOS ===");

        // 1. SCELTA RETE (Questa logica ce l'hai già!)
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

                RmiClient rmiClient = new RmiClient(serverStub, localModel, controller);
                serverStub.connect(nicknameUtente, rmiClient);
                System.out.println("Connesso con successo via RMI!");
            } else {
                System.out.println("La connessione Socket non è ancora implementata!");
                return; // Esce dal programma per ora
            }

            // SETUP VIEW (I famosi Placeholder)
            if (uiChoice == 1) {
                System.out.println("Avvio della TUI...");
                // TODO: TuiView tui = new TuiView(localModel, controller);
                // TODO: tui.start();
            } else {
                System.out.println("Avvio della GUI...");
                // TODO: Application.launch(GuiView.class, args); (se userete JavaFX)
            }

        } catch (Exception e) {
            System.err.println("Errore fatale: " + e.getMessage());
        }
    }
}
