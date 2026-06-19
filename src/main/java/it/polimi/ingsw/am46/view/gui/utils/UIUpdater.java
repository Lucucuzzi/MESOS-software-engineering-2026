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
 * Manages throttling of GUI updates.
 * Strategy: debounce.
 * - Always saves the latest received GameState.
 * - Defers the update by DEBOUNCE_MS milliseconds.
 * - If a new state arrives meanwhile, it overwrites the previous one and resets the timer.
 * - When the timer fires, applies the latest state on the JavaFX Application Thread.
 */


/**
 * The type Ui updater.
 */
public class UIUpdater {

    // Debounce window in milliseconds
    private static final long DEBOUNCE_MS = 120;
    // Latest received state (intermediate ones are overwritten)
    private final AtomicReference<GameState> pendingState = new AtomicReference<>();
    // Function that applies the state to the GUI (must run on JavaFX thread)
    private final Consumer<GameState> onUpdate;

    // Scheduler for deferred execution
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
     * Called from the network thread when a new GameState arrives.
     * Never touches the GUI directly.
     */
    public void submit(GameState newState) {
        // Always save the latest state
        pendingState.set(newState);
        synchronized (taskLock) {
            // Cancel the previous task if still pending
            if (pendingTask != null && !pendingTask.isDone()) {
                pendingTask.cancel(false);
            }
            // Schedule a new task after the debounce window
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
     * Forces an immediate update, bypassing the debounce.
     * Useful for critical errors or end of game.
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
//Shuts down the scheduler when the GUI is closed.
    public void shutdown() {
        scheduler.shutdownNow();
    }
}






