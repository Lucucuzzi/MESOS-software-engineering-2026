package it.polimi.ingsw.am46.network.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;



// Questa coda è "agnostica" rispetto al protocollo: non sa se sta parlando con Socket o RMI.
// Sfrutta il polimorfismo: riceve un Runnable (il comando) e lo esegue nel suo thread.
// Se il server è un proxy Socket, il Runnable impacchetterà JSON; se è uno stub RMI,
// gestirà la chiamata remota. La coda si occupa solo dell'ordine e dell'asincronia,
// lasciando i dettagli della comunicazione alle classi specifiche.


/**
 * The type Command queue.
 */
// Questa classe serve a rendere asincrone le chiamate dal Client verso il Server.
// È fondamentale per RMI: evita che la GUI si blocchi mentre aspetta la risposta della rete.
public class CommandQueue {

    // La coda LinkedBlockingQueue garantisce due cose:
    // 1. Ordine FIFO: le mosse arrivano al server nell'ordine esatto in cui l'utente ha cliccato.
    // 2. Thread-safety: permette alla GUI di aggiungere comandi mentre il worker li estrae.
    // Accetta azioni dal server, piccoli pezzi di codice (runnable)
    private final LinkedBlockingQueue<Runnable> queue
            = new LinkedBlockingQueue<>();


    // Questo Consumer è la nostra "linea diretta" con la UI in caso di problemi.
    // Viene usato per inviare messaggi di errore (come "Server irraggiungibile").
    private final Consumer<String> onError;

    // Marcata come 'volatile' per garantire che il thread worker legga subito il cambio
    // di valore quando viene chiamato lo shutdown(), evitando che resti attivo inutilmente.
    private volatile boolean running = true;

    // Usiamo un SINGOLO thread dedicato (Worker).
    // Perché uno solo? Se ne usassimo molti (Thread Pool), le mosse potrebbero arrivare
    // disordinate al server (es: il "Passo turno" arriva prima del "Muovo Totem").
    private final Thread worker;

    /**
     * Instantiates a new Command queue.
     *
     * @param onError the on error
     */
    public CommandQueue(Consumer<String> onError) {
        this.onError = onError;
        // Inizializziamo il thread che eseguirà i comandi in background.
        this.worker = new Thread(this::processLoop, "command-queue-worker");

        // Impostato come Daemon: se l'utente chiude il gioco, questo thread muore subito
        // e non lascia processi "fantasma" appesi nel sistema.
        this.worker.setDaemon(true);
        this.worker.start();
    }

    /**
     * Submit.
     *
     * @param command the command
     */
// Questo è il metodo chiamato dalla GUI (ClientController).
    // È istantaneo: "parcheggia" la mossa nella coda e libera subito il thread della UI.
    public void submit(Runnable command) {
        queue.offer(command);
    }


    // Ciclo infinito che gira nel thread in background (il "motore" della coda).
    private void processLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Il thread si mette in pausa qui (consumo CPU zero) finché non c'è una mossa.
                // Appena arriva un comando, 'take' lo preleva e il thread si sveglia.
                Runnable cmd = queue.take();
                try {
                    // Esegue l'effettiva chiamata di rete (RMI o Socket).
                    // Qui avviene l'eventuale attesa se la rete è lenta.
                    cmd.run();
                } catch (Exception e) {
                    // Network error or server rejection
                    if (onError != null) {
                        onError.accept(e.getMessage() != null
                                ? e.getMessage()
                                : "Errore di rete sconosciuto");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        running = false;
        worker.interrupt();
    }

}
