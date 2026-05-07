package it.polimi.ingsw.am46.view.cli;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.*;
import it.polimi.ingsw.am46.view.cli.handler.CLIInputHandler;
import it.polimi.ingsw.am46.view.cli.display.CLIDisplayManager;
import it.polimi.ingsw.am46.view.cli.utils.ColorCode;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CLIView
 *
 * Implementation of GameView for the text-based interface.
 * RESPONSIBILITIES:
 * - Coordinate InputHandler and DisplayManager
 * - Manage the lifecycle (start, stop)
 * - Implement the Observer pattern (onStateUpdate, onError)
 * - Synchronize display access across threads
 * - Manage the CountDownLatch to unlock the main thread
 * THREADING:
 * - MAIN THREAD: remains blocked in ClientLauncher.latch.await()
 * - INPUT THREAD: created by executor to read commands
 * - NETWORK THREAD: (external) calls onStateUpdate() and onError()
 * SYNCHRONIZATION:
 * - screenLock: ReentrantReadWriteLock protecting stdout
 * - waitingForInput: AtomicBoolean indicating if the user is typing
 * - latch: CountDownLatch unlocking the main thread when view.stop() is called
 */
public class CLIView implements GameView {

    // ====
    // DEPENDENCIES
    // ==========
    private final CountDownLatch latch; // Unlocks the main thread when CLI closes

    // ===
    // THREADS & SYNCHRONIZATION
    // =====
    // read the input, blocking action
    private final Scanner scanner;
    private final Thread inputThread; // Creates a single background thread for input reading
    private final ReentrantReadWriteLock screenLock; // Synchronizes stdout
    private final AtomicBoolean waitingForInput; // Flag checking if user is typing prevent race conditions

    // ======
    // DELEGATES
    // ======
    private final CLIInputHandler inputHandler;
    private final CLIDisplayManager displayManager;

    // ===
    // STATE
    // ====================
    private volatile boolean running;
    private volatile GameState currentState;

    /**
     * Constructor.
     *
     * @param localModel state cache
     * @param controller controller sending commands to the server
     * @param latch the CountDownLatch that unlocks the main thread
     */
    public CLIView(LocalModel localModel, ClientController controller, CountDownLatch latch) {
        this.latch = latch;
        this.scanner = new Scanner(System.in);

        // output thread that will read the user input, it is a daemon thread because we want it to end when the main thread ends
        this.inputThread = new Thread(() -> this.inputLoop(), "cli-input-thread");
        this.inputThread.setDaemon(true);
        // not a user thread, when all the user threads finish this thread ends too

        this.screenLock = new ReentrantReadWriteLock();
        this.waitingForInput = new AtomicBoolean(false);
        this.running = true;

        this.inputHandler = new CLIInputHandler(controller);
        this.displayManager = new CLIDisplayManager(screenLock, localModel);

        setupCallbacks();
    }

    /**
     * Sets up callbacks between InputHandler and DisplayManager.
     * This allows the two delegates to communicate without direct coupling.
     */
    private void setupCallbacks() {
        // InputHandler → DisplayManager: feedback messages
        // the message is created and passed in InputHandler.
        inputHandler.setDisplayCallback(message -> displayManager.displayMessage(message));

        // InputHandler → DisplayManager: display commands
        // These already used lambdas because they capture the 'currentState' variable.
        inputHandler.setOnStatusRequested(() -> displayManager.displayStatus(currentState));
        inputHandler.setOnBoardRequested(() -> displayManager.displayBoard(currentState));

        // Expects a Runnable. We use () to indicate no parameters are passed.
        inputHandler.setOnHelpRequested(() -> displayManager.displayHelp());

        // InputHandler → CLIView: quit
        // Expects a Runnable. We use () to invoke the instance method explicitly.
        inputHandler.setOnQuitRequested(() -> {
            System.out.println("Uscita richiesta dall'utente...");
            this.latch.countDown(); // Unlocks the main thread to allow the application to exit
        });
    }


    // =====================
    // GameView IMPLEMENTATION
    // ========

    //called by ClientLauncher after the creation of the view
    @Override
    public void start() {
        displayManager.displayWelcome();
        displayManager.displayHelp();
        // Starts the thread. it will detach from the main thread and execute the inputLoop in background
        inputThread.start();
    }

    @Override
    public void stop() {
        running = false; //while(running) ends
        inputHandler.stop();
        inputThread.interrupt(); // Interrupts the input thread
        scanner.close();

        screenLock.writeLock().lock(); //only this thread can write or read the screen
        try {
            System.out.println();
            System.out.println(ColorCode.info(" Thank you for playing MESOS!"));
        } finally {
            screenLock.writeLock().unlock();
        }

        latch.countDown(); // Unlocks the main thread
    }

    @Override
    public void showMessage(String message) {
        displayManager.displayMessage(message);
    }

    @Override
    public void drawBoard(GameState state) {
        if (state != null) {
            displayManager.displayBoard(state);
        }
    }

    @Override
    public void onStateUpdate(GameState newState) {
        this.currentState = newState;
        refresh(newState);
    }

    @Override
    public void onError(String errorMessage) {
        screenLock.writeLock().lock();
        try {
            System.out.println();
            System.out.println(ColorCode.error("X ERROR: " + errorMessage));
            if (waitingForInput.get()) {
                System.out.print(ColorCode.BRIGHT_YELLOW + ">" + ColorCode.RESET);
                System.out.flush();
            }
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    // ====
    // INPUT LOOP
    // =======

    /**
     * Main input loop.
     * Executed in a separate thread (executor).
     */
    private void inputLoop() {
        // We check !Thread.currentThread().isInterrupted() in addition to 'running'
        // to guarantee immediate termination if stop() interrupts the thread.
        // We also keep the 'running' flag to distinguish a graceful shutdown (like typing "quit")
        // from an unexpected crash, allowing us to safely ignore scanner exceptions in the catch block.
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 1. Print prompt
                screenLock.writeLock().lock();
                try {
                    System.out.print(ColorCode.BRIGHT_YELLOW + ">" + ColorCode.RESET);
                    System.out.flush();
                } finally {
                    screenLock.writeLock().unlock();
                }

                // 2. Read input (OUTSIDE the lock - blocking!)
                waitingForInput.set(true);
                String input = scanner.nextLine().trim(); // .trim() removes whitespace from both ends of this string
                waitingForInput.set(false);

                // 3. Process if not empty
                if (!input.isEmpty()) {
                    inputHandler.handleInput(input);
                }

            } catch (Exception e) {
                if (running) { // Ignore exceptions during shutdown
                    showMessage(ColorCode.error(" Error: " + e.getMessage()));
                }
                break;
            }
        }
    }


    /**
     * Smart refresh when the state changes.
     * If the user is typing, it shows only a lightweight notification.
     * If the user is not typing, it redraws the full board.
     */
    private void refresh(GameState state) {
        if (waitingForInput.get()) {
            // User is typing - lightweight notification
            screenLock.writeLock().lock();
            try {
                System.out.println("\n" + ColorCode.success(" State updated!") +
                        " (type 'board' to view)");
                System.out.print(ColorCode.BRIGHT_YELLOW + ">" + ColorCode.RESET);
                System.out.flush();
            } finally {
                screenLock.writeLock().unlock();
            }
        } else {
            // User is not typing - redraw full board
            drawBoard(state);
        }
    }

    @Override
    public void onAbort(String errorMessage) {
        System.err.println("\n[FATAL ERROR]" + errorMessage);

        // Unlock il main thread of ClientLauncher
        stop();
        System.exit(0);
    }
}