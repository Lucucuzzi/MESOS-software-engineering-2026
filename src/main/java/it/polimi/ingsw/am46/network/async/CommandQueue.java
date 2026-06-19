package it.polimi.ingsw.am46.network.async;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;



// This queue is "protocol agnostic": it does not know whether it is talking to Socket or RMI.
// It takes advantage of polymorphism: it receives a Runnable (the command) and executes it in its thread.
// If the server is a Socket proxy, the Runnable will package JSON; if it is an RMI stub,
// will handle the remote call. The queue only takes care of the order and asynchrony,
// leaving the details of the communication to the specific classes.


/**
 * The type Command queue.
 */
// This class is used to make calls from the Client to the Server asynchronous.
// It is essential for RMI: it prevents the GUI from blocking while waiting for the network response.
public class CommandQueue {

    // The LinkedBlockingQueue ensures two things:
    // 1. FIFO order: moves arrive at the server in the exact order the user clicked.
    // 2. Thread-safety: allows the GUI to add commands while the worker extracts them.
    // Accept actions from the server, small pieces of code (runnable)
    private final LinkedBlockingQueue<Runnable> queue
            = new LinkedBlockingQueue<>();


    // This Consumer is our "direct line" to the UI in case of problems.
    // It is used to send error messages (such as "Server unreachable").
    private final Consumer<String> onError;

    // Marked as 'volatile' to ensure that the worker thread immediately reads the value change
    // when shutdown() is called, preventing it from remaining active to no avail.
    private volatile boolean running = true;

    // We use a SINGLE dedicated thread (Worker).
    // Why just one? If we used many of them (Thread Pool), the moves could arrive
    // out of order at the server (e.g. the "Turn Step" arrives before the "Totem Move").
    private final Thread worker;

    /**
     * Instantiates a new Command queue.
     *
     * @param onError the on error
     */
    public CommandQueue(Consumer<String> onError) {
        this.onError = onError;
        // We initialize the thread that will execute commands in the background.
        this.worker = new Thread(this::processLoop, "command-queue-worker");

        // Set as Daemon: if the user closes the game, this thread dies immediately
        // and leaves no "ghost" processes hanging in the system.
        this.worker.setDaemon(true);
        this.worker.start();
    }

    /**
     * Submit.
     *
     * @param command the command
     */
// This is the method called by the GUI (ClientController).
    // It is instantaneous: it ‘parks’ the move in the queue and immediately frees up the UI thread.
    public void submit(Runnable command) {
        queue.offer(command);
    }


    // Infinite loop running in the background thread (the "engine" of the queue).
    private void processLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // The thread pauses here (zero CPU consumption) until there is a move.
                // As soon as a command arrives, 'take' picks it up and the thread wakes up.
                Runnable cmd = queue.take();
                try {
                    // Execute the actual network call (RMI or Socket).
                    // Any waiting happens here if the network is slow.
                    cmd.run();
                } catch (Exception e) {
                    // Network error or server rejection
                    if (onError != null) {
                        onError.accept(e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown network error");
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
