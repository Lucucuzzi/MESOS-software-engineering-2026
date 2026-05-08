package it.polimi.ingsw.am46.network.rmi.server;

import it.polimi.ingsw.am46.controller.ServerController;
import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.network.async.AsyncBroadcastManager;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

/*
 * Concrete implementation of the RMI server.
 * Extends UnicastRemoteObject → automatically creates the skeleton,
 * sets the server to listen on the network, and makes the methods
 * remotely invocable.
 * Implements VirtualServerRmi → receives commands from clients.
 * Implements VirtualView → manages broadcasts to observers.
 * It serves as both the command entry point (VirtualServerRmi)
 * and the Broadcast Manager (VirtualView).
 */

public class RmiServer extends UnicastRemoteObject
        implements VirtualServerRmi, VirtualView {

    // ServerController reference to call the game logic methods (connect, moveTotem, etc.)
    private final ServerController controller;

    // Sostituiamo la vecchia mappa Map<String, VirtualViewRmi> con il Manager.
    // Il manager non è solo un contenitore, ma un sistema attivo che gestisce
    // i thread di invio per ogni singolo client registrato.
    private final AsyncBroadcastManager broadcastManager;

    // per verificare che la connessione RMI sia ancora attiva "sotto il cofano".
    private final ScheduledExecutorService pingScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                // Creiamo un thread dedicato al ping e lo chiamiamo "rmi-ping"
                // per trovarlo facilmente nel debugger se ci sono problemi.
                Thread t = new Thread(r, "rmi-ping");
                // Daemon = true significa che se il server si chiude, questo thread
                // non rimane appeso a bloccare il computer.
                t.setDaemon(true);
                return t;
            });

    public RmiServer(ServerController controller) throws RemoteException {
        super();
        this.controller = controller;
        // Pass disconnection handling to the broadcast manager
        this.broadcastManager = new AsyncBroadcastManager(
                // Inizializziamo il manager asincrono.
                // Fondamentale: gli passiamo il riferimento al metodo 'handleDisconnection'
                // del controller. In questo modo, se il manager scopre che un client è morto
                // mentre provava a inviargli un update, può avvisare il gioco automaticamente.
                controller::handleDisconnection
        );
        // Facciamo partire il ciclo di ping periodico verso i client.
        startPing();
    }

    // =========================================================
    // VirtualServerRmi - Ricezione comandi dai Client
    // =========================================================

    @Override
    public void connect(String nickname, String colorName, VirtualViewRmi cur) throws RemoteException, GameAlreadyStartedException, InvalidConnectionException {

        // Passiamo nickname, colore e vista al controller (3 parametri)
        // come richiesto dalla logica di business del tuo ServerController
        controller.connect(nickname, colorName, cur);

        // Registriamo il client nel manager asincrono (serve solo nick e vista)
        broadcastManager.registerClient(nickname, cur);

    }
    @Override
    public List<String> getAvailableColors() throws RemoteException, Exception {
        var availableEnums = controller.getAvailableColors();

        // 2. Li trasformiamo in stringhe per mandarle via rete in modo "stupido"
        List<String> stringColors = new ArrayList<>();
        for (var c : availableEnums) {
            stringColors.add(c.name());
        }
        return stringColors;
    }

    @Override
    public void setExpectedPlayers(String nickname, int numPlayers) throws RemoteException {
        controller.setExpectedPlayers(nickname, numPlayers);
    }



    @Override
    public void moveTotem(String nickname, String offerTileId) throws RemoteException {
        controller.moveTotem(nickname, offerTileId);
    }

    @Override
    public void addCard(String nickname, String cardId) throws RemoteException {
        controller.addCard(nickname, cardId);
    }

    @Override
    public void addExtraCard(String nickname, String cardId) throws RemoteException {
        controller.addExtraCard(nickname, cardId);
    }

    @Override
    public void skipExtraDraw(String nickname) throws RemoteException {
        controller.skipExtraDraw(nickname);
    }


    // =========================================================
    // VirtualView — sends notifications to clients
    // =========================================================

    @Override
    public synchronized void registerClient(String nickname, NetworkMode cur) {
        // Invece di controllare la classe con instanceof, chiediamo all'oggetto
        // stesso se rappresenta una connessione Socket o RMI.
        try {
            if (!cur.isSocket()) {
                // Se non è un socket, per esclusione in questo progetto è un client RMI.
                // Facciamo il cast a VirtualViewRmi per passarlo al manager.
                // Il cast è sicuro perché abbiamo appena verificato la natura del network.
                VirtualViewRmi rmiView = (VirtualViewRmi) cur;
                broadcastManager.registerClient(nickname, rmiView);
            }
            // Se cur.isSocket() è true, non facciamo nulla:
            // questo è il server RMI e non deve gestire client Socket.
        } catch (RemoteException e) {
            // Gestiamo l'eventuale errore di comunicazione durante il controllo
            System.err.println("[RMI] Errore durante la verifica del tipo di network per: " + nickname);
        }
    }


    // registerClient and unregisterClient are synchronized to prevent data races on clients
    @Override
    public synchronized void unregisterClient(String nickname) {
        broadcastManager.unregisterClient(nickname);
    }


    @Override
    public void broadcastUpdate(GameState gameState) {
        // Metodo NON BLOCCANTE: non invia fisicamente i dati ora, ma li "parcheggia"
        // nelle code del manager. Ritorna in microsecondi, permettendo al server
        // di tornare subito a gestire la logica di gioco senza aspettare i client.
        broadcastManager.broadcastUpdate(gameState);
    }

    @Override
    public void sendError(String nickname, String errorMessage) {
        // Gli errori sono critici: se un utente fa una mossa non valida, deve saperlo subito.
        // Recuperiamo il riferimento (lo stub) del client direttamente dal manager.
        VirtualViewRmi view = broadcastManager.getView(nickname);

        if (view != null) {
            // Creiamo un thread "usa e getta" solo per questo errore.
            // Perché? Perché non vogliamo intasare la coda dei messaggi di gioco (broadcast)
            // con messaggi d'errore, ma vogliamo comunque evitare che il server si blocchi
            // se la rete del client è lenta in questo istante.
            new Thread(() -> {
                try {
                    view.signalError(errorMessage);
                } catch (RemoteException e) {
                    // Se la chiamata fallisce, il client è probabilmente crashato.
                    // Lo rimuoviamo dal manager e avvisiamo il controller per gestire la pulizia.
                    broadcastManager.unregisterClient(nickname);
                    controller.handleDisconnection(nickname);
                }
            }, "error-send-" + nickname).start();
        }
    }

    @Override
    public void broadcastError(String errorMessage) {
        // Non serve più creare una lista locale o usare synchronized(this).
        // Il broadcastManager gestisce internamente la lista dei client in modo thread-safe.

        // Usiamo un thread separato per il broadcast dell'errore.
        // Perché? Perché gli errori (es. "Il server sta per chiudersi") spesso devono
        // viaggiare su una corsia preferenziale e non restare accodati dietro a
        // pesanti aggiornamenti del GameState.
        new Thread(() -> {
            // Chiediamo al manager di inviare il messaggio a tutti i client RMI registrati.
            // Il manager eseguirà le chiamate in parallelo o sequenziale nei suoi thread,
            // isolando eventuali crash dei singoli client.
            broadcastManager.broadcastError(errorMessage);
        }, "broadcast-error-thread").start();
    }

    @Override
    public void broadcastWinner(GameState finalState) {
        // Winner notification — goes through the normal async queue
        broadcastManager.broadcastUpdate(finalState);
    }

    @Override
    public void clearClients() {
        // Chiediamo al manager di pulire tutto
        broadcastManager.clearClients();
    }

    @Override
    public void broadcastAbort(String reason) {
        // Delega al manager l'invio asincrono a tutti i client RMI.
        // Il server RMI torna subito disponibile per altre operazioni.
        broadcastManager.broadcastAbort(reason);
    }

    private void startPing() {
        pingScheduler.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, VirtualViewRmi> entry : broadcastManager.getAllViews().entrySet()) {
                String nick = entry.getKey();
                VirtualViewRmi view = entry.getValue();
                try {
                    view.ping();
                } catch (RemoteException e) {
                    broadcastManager.unregisterClient(nick);
                    controller.handleDisconnection(nick);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        broadcastManager.shutdown();
        pingScheduler.shutdownNow();
    }


}
