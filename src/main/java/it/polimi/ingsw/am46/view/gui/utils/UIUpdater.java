package it.polimi.ingsw.am46.view.gui.utils;
import it.polimi.ingsw.am46.network.dto.GameState;
import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/*
 * Gestisce il throttling degli aggiornamenti della GUI.
 * Strategia: debounce.
 * - Salva sempre l'ultimo GameState ricevuto.
 * - Rimanda l'aggiornamento di DEBOUNCE_MS millisecondi.
 * - Se nel frattempo arriva un nuovo stato, sovrascrive il precedente e resetta il timer.
 * - Quando il timer scade, applica l'ultimo stato sul JavaFX Application Thread.
 */


/**
 * The type Ui updater.
 */
public class UIUpdater {

    // Finestra di debounce in millisecondi
    private static final long DEBOUNCE_MS = 120;
    // Ultimo stato ricevuto (gli intermedi vengono sovrascritti)
    private final AtomicReference<GameState> pendingState = new AtomicReference<>();
    // Funzione che applica lo stato alla GUI (deve essere eseguita sul JavaFX thread)
    private final Consumer<GameState> onUpdate;

    // Scheduler per programmare l'esecuzione differita
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ui-updater-thread");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> pendingTask;
    private final Object taskLock = new Object();

    /**
     * Instantiates a new Ui updater.
     *
     * @param onUpdate the on update
     */
    public UIUpdater(Consumer<GameState> onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * Submit.
     *
     * @param newState the new state
     */
    /*
     * Chiamato dal thread di rete quando arriva un nuovo GameState.
     * Non tocca mai direttamente la GUI.
     */
    public void submit(GameState newState) {
        // Salva sempre l'ultimo stato
        pendingState.set(newState);
        synchronized (taskLock) {
            // Annulla il task precedente, se ancora in attesa
            if (pendingTask != null && !pendingTask.isDone()) {
                pendingTask.cancel(false);
            }
            // Pianifica un nuovo task dopo la finestra di debounce
            pendingTask = scheduler.schedule(() -> {
                GameState latest = pendingState.getAndSet(null);
                if (latest != null) {
                    Platform.runLater(() -> onUpdate.accept(latest));
                }
            }, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Submit immediate.
     *
     * @param state the state
     */
    /*
     * Forza un aggiornamento immediato, bypassando il debounce.
     * Utile per errori critici o fine partita.
     */
    public void submitImmediate(GameState state) {
        synchronized (taskLock) {
            if (pendingTask != null) {
                pendingTask.cancel(false);
            }
        }
        pendingState.set(null);
        Platform.runLater(() -> onUpdate.accept(state));
    }


    /**
     * Shutdown.
     */
//Arresta lo scheduler quando la GUI viene chiusa.
    public void shutdown() {
        scheduler.shutdownNow();
    }
}






