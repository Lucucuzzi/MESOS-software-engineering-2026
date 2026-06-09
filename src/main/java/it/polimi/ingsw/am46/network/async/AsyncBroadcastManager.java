package it.polimi.ingsw.am46.network.async;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/*
 Async broadcast manager for RMI clients.

 Each registered client gets its own bounded queue (capacity=2) and its own dedicated delivery thread.

 Why bounded queue with capacity 2?
 The server can send multiple GameStates very quickly during automatic
 phase transitions (ResolveEvents → EndRound → PlaceTotem fires 3 updates
 in milliseconds). A slow client shouldn't accumulate all of them —
 only the latest state matters. When the queue is full, old states
 are evicted and replaced with the newest one.

 Why one thread per client?
 If a single dispatch thread serves all clients sequentially, one slow
 client blocks delivery to everyone. With one thread per client,
 Carlo's 3-second lag affects only Carlo.
 */

// NOTA: Questo manager serve esclusivamente per gestire la natura sincrona di RMI.
// A differenza dei Socket, dove l'invio è gestito dai buffer TCP del sistema operativo,
// in RMI ogni chiamata broadcastUpdate() bloccherebbe il server finché il client non risponde.
// Se un client RMI ha lag, fermerebbe la partita per tutti. Questa classe isola ogni
// client RMI in un thread dedicato (il deliveryLoop), garantendo che i ritardi di un
// singolo giocatore non influenzino minimamente gli altri o il resto del server.

/**
 * The type Async broadcast manager.
 */
public class AsyncBroadcastManager {
    // Usiamo una capacità molto piccola (2). Perché?
    // In un gioco online, se un client è lento, non serve mandargli 10 stati vecchi.
    // Gli manderemo solo l'ultimo disponibile per tenerlo aggiornato in tempo reale.
    private static final int QUEUE_CAPACITY = 2;

    // ConcurrentHashMap è fondamentale: permette a più thread di aggiungere/rimuovere
    // client contemporaneamente senza mandare in crash il server.
    private final Map<String, ClientChannel> channels
            = new ConcurrentHashMap<>();


    /* Questo componente è fondamentale per garantire che il server sia robusto e non sprechi risorse.
    Senza questo DisconnectionHandler,
    il server continuerebbe a cercare di parlare con "fantasmi" (client che non esistono più).
     */
    private final DisconnectionHandler onDisconnected;

    /**
     * The interface Disconnection handler.
     */
    @FunctionalInterface
    public interface DisconnectionHandler {
        /**
         * Handle.
         *
         * @param nickname the nickname
         */
        void handle(String nickname);
    }

    /**
     * Instantiates a new Async broadcast manager.
     *
     * @param onDisconnected the on disconnected
     */
    public AsyncBroadcastManager(DisconnectionHandler onDisconnected) {
        this.onDisconnected = onDisconnected;
    }


    /**
     * Register client.
     *
     * @param nickname the nickname
     * @param view     the view
     */
//Registers a new RMI client and starts its delivery thread.
     //Called by RmiServer when a client connects.
    public void registerClient(String nickname, VirtualViewRmi view) {
        // Ogni client ha la sua coda personale. Se la sua rete è lenta,
        // si riempie solo la SUA coda, non quella degli altri.
        /* Usiamo LinkedBlockingQueue per tre motivi fondamentali:
           1. THREAD-SAFETY: Il ServerController scrive nella coda mentre il thread
           postino legge. Questa struttura gestisce i conflitti internamente.
           2. EFFICIENZA (Blocking): Se la coda è vuota, il thread si mette in pausa
           automaticamente (non consuma CPU) finché non arriva un nuovo aggiornamento.
           3. GESTIONE DEI LAG: Avendo una capacità fissa, se un client è troppo lento
           possiamo scartare i dati vecchi e tenere solo quelli recenti.
         */
        LinkedBlockingQueue<GameState> queue
                = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // Creiamo un thread dedicato (il "postino") per questo specifico client.
        // In questo modo, se la sua rete rallenta, bloccherà solo questo thread
        // e non l'intero server o gli altri giocatori.
        Thread thread = new Thread(
                () -> deliveryLoop(nickname, view, queue),
                "rmi-delivery-" + nickname
        );

        // Impostato come Daemon: se il server si spegne, questi thread di servizio
        // non devono impedire la chiusura del processo
        thread.setDaemon(true);
        thread.start();

        // Salviamo il "canale" completo nella mappa per poterlo recuperare
        // quando dobbiamo fare un broadcast o gestire una disconnessione.
        channels.put(nickname, new ClientChannel(queue, thread, view));
    }


     //Removes a client and interrupts its delivery thread.

    /**
     * Unregister client.
     *
     * @param nickname the nickname
     */
    public void unregisterClient(String nickname) {
        ClientChannel ch = channels.remove(nickname);
        if (ch != null) {
            // Fermiamo il thread postino immediatamente.
            ch.thread.interrupt();
        }
    }

    /**
     * Broadcast update.
     *
     * @param state the state
     */
// Spedisce il nuovo stato del gioco a tutti i giocatori connessi.
    // L'operazione è istantanea perché non "parla" con la rete, ma scrive solo nella memoria locale.
    public void broadcastUpdate(GameState state) {
        // Cicliamo su tutti i canali attivi nella nostra mappa ConcurrentHashMap.
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            // Per ogni client, mettiamo lo stato nella sua coda personale.
            enqueue(entry.getValue().queue, state);
        }
    }

    /**
     * Send to one.
     *
     * @param nickname the nickname
     * @param state    the state
     */
// Invia un aggiornamento mirato a un singolo giocatore (es. per una riconnessione o un errore privato).
    public void sendToOne(String nickname, GameState state) {
        // Recuperiamo il "pacchetto" (coda + thread) associato al nickname.
        ClientChannel ch = channels.get(nickname);
        if (ch != null) {
            enqueue(ch.queue, state);
        }
    }

    /**
     * Broadcast error.
     *
     * @param errorMessage the error message
     */
    public void broadcastError(String errorMessage) {
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            String nickname = entry.getKey();
            ClientChannel ch = entry.getValue();

            // NON usiamo ch.queue.offer perché la coda accetta solo GameState.
            // Creiamo un thread rapido "usa e getta" per inviare l'errore subito.
            new Thread(() -> {
                try {
                    ch.view.signalError(errorMessage);
                } catch (RemoteException e) {
                    // Se fallisce, puliamo la connessione
                    unregisterClient(nickname);
                    onDisconnected.handle(nickname);
                }
            }, "error-sender-" + nickname).start();
        }
    }

    /**
     * Broadcast abort.
     *
     * @param reason the reason
     */
    public void broadcastAbort(String reason) {
        // Cicliamo su tutti i canali connessi
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            String nickname = entry.getKey();
            ClientChannel ch = entry.getValue();

            // Creiamo un thread dedicato per ogni client.
            // Non usiamo la coda perché l'abort deve bypassare i GameState pendenti.
            new Thread(() -> {
                try {
                    ch.view.abortGame(reason);
                } catch (RemoteException e) {
                    // Se il client non risponde, lo disconnettiamo formalmente
                    unregisterClient(nickname);
                    onDisconnected.handle(nickname);
                }
            }, "abort-sender-" + nickname).start();
        }
    }

    /**
     * Clear clients.
     */
    public void clearClients() {
        // 1. Fermiamo tutti i thread di invio (processLoop) per ogni client
        for (ClientChannel ch : channels.values()) {
            ch.thread.interrupt();
        }

        // 2. Svuotiamo la mappa dei canali
        channels.clear();

        System.out.println("[Manager] Tutti i client sono stati rimossi e i thread chiusi.");
    }

    /**
     * Gets view.
     *
     * @param nickname the nickname
     * @return the view
     */
// Restituisce lo stub RMI (la "vista remota") di un giocatore specifico.
    // Viene usato dal server per inviare comunicazioni dirette, come messaggi d'errore
    // o segnali di "partita iniziata" che non passano necessariamente per la coda di broadcast.
    public VirtualViewRmi getView(String nickname) {
        ClientChannel ch = channels.get(nickname);
        return ch != null ? ch.view : null;
    }


    /**
     * Gets client count.
     *
     * @return the client count
     */
// È utile per il server per sapere se la lobby è piena o se ci sono abbastanza giocatori.
    public int getClientCount() {
        return channels.size();
    }

    /**
     * Shutdown.
     */
// Spegne l'intero sistema di trasmissione asincrona.
    // Viene chiamato quando il server viene chiuso per non lasciare thread "orfani".
    public void shutdown() {
        // Cicliamo su tutti i canali dei client attualmente connessi.
        for (ClientChannel ch : channels.values()) {
            // Inviamo un segnale di interruzione a ogni thread "postino".
            // Questo farà uscire i thread dal loro deliveryLoop in modo pulito.
            ch.thread.interrupt();
        }
        // Svuotiamo la mappa: a questo punto il server non ha più client registrati.
        channels.clear();
    }


    // Gestisce l'inserimento intelligente nella coda (politica di rimpiazzo).
    private void enqueue(LinkedBlockingQueue<GameState> queue, GameState state) {
        // Proviamo a inserire lo stato. Se la coda è piena (offer restituisce false)...
        if (!queue.offer(state)) {
            // ...rimuoviamo lo stato più vecchio presente (poll) per fare spazio...
            queue.poll();
            // ...e inseriamo quello più recente. Questo garantisce che il client
            // riceva sempre l'ultima situazione del tavolo e non dati obsoleti.
            queue.offer(state);
        }
    }

    /**
     * Gets all views.
     *
     * @return the all views
     */
    public Map<String, VirtualViewRmi> getAllViews() {
        Map<String, VirtualViewRmi> result = new HashMap<>();
        for (Map.Entry<String, ClientChannel> entry : channels.entrySet()) {
            result.put(entry.getKey(), entry.getValue().view());
        }
        return result;
    }



    // Questo metodo è il lavoro svolto dal thread "postino" di ogni client.
    private void deliveryLoop(String nickname,
                              VirtualViewRmi view,
                              LinkedBlockingQueue<GameState> queue) {
        try {
            // Il thread continua a girare finché non viene interrotto esplicitamente.
            while (!Thread.currentThread().isInterrupted()) {
                // Il thread si mette in pausa qui (senza consumare CPU) finché non arriva
                // qualcosa nella coda. Appena arriva uno stato, "take" lo preleva.
                GameState state = queue.take();
                try {
                    // Chiamata RMI reale verso il client. È qui che avviene il potenziale ritardo.
                    // Se la rete è lenta, solo questo specifico thread rimarrà fermo ad aspettare.
                    view.updateView(state);
                } catch (RemoteException e) {
                    // Se la chiamata fallisce, significa che il client è crashato o offline.
                    System.err.println("[Broadcast] "
                            + nickname + " disconnected: " + e.getMessage());
                    // Avvisiamo il ServerController della disconnessione per ripulire la partita.
                    onDisconnected.handle(nickname);
                    // Usciamo dal metodo: questo terminerà definitivamente il thread postino.
                    return;
                }
            }
        } catch (InterruptedException e) {
            // Se il thread viene interrotto mentre aspetta sulla coda, usciamo puliti.
            Thread.currentThread().interrupt();
        }
    }

    // Struttura dati immutabile che raggruppa tutto ciò che serve per gestire un client.
    // Usiamo un record per chiarezza: contiene la coda dei messaggi,
    // il thread che li spedisce e lo stub RMI (view) per contattare il client.
    private record ClientChannel(
            LinkedBlockingQueue<GameState> queue,
            Thread thread,
            VirtualViewRmi view
    ) {}
}
