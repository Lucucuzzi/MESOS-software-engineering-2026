package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.dto.GameState;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Client-side local cache of the game state.
 * It is updated ONLY when a GameState is received from the server.
 * It does not contain any game logic—it is merely a snapshot.
 * Implements the Observer Pattern:
 * maintains a list of ModelObservers (the Views)
 * and notifies them when the state changes.
 */


/**
 * The type Local model.
 */
public class LocalModel {

    //The current GameState received from the server
    private GameState currentState;

    // List of observers (Views) registered on the client
    // We use CopyOnWriteArrayList instead of a standard ArrayList with Lock/Synchronized for two main reasons:
// 1. ELIMINATION OF CONCURRENT-MODIFICATION: The Copy-On-Write pattern guarantees that each iterator
//    works on a snapshot of the list. This avoids crashes (ConcurrentModificationException) if an
//    observer registers or disconnects while the system is iterating over the list to send
//    a state update.
// 2. READ PERFORMANCE: In this application, reads (observer notifications) are extremely
//    frequent, while writes (observer registration) happen almost only during the initial phase.
//    By removing 'synchronized' blocks, we allow multiple threads (RMI, Socket and UI) to access the list
//    simultaneously without slowdowns or bottlenecks.
    private final CopyOnWriteArrayList<ModelObserver> observers = new CopyOnWriteArrayList<>();

    // Add the field at the top of the class alongside the others
    private volatile boolean reconnectConfirmed = false;

    /**
     * Notify reconnect confirmed.
     */
// Add these two methods
    public void notifyReconnectConfirmed() {
        this.reconnectConfirmed = true;
    }

    /**
     * Is reconnect confirmed boolean.
     *
     * @return the boolean
     */
    public boolean isReconnectConfirmed() {
        return reconnectConfirmed;
    }

    // Best practice: A private lock object encapsulates synchronization,
    // preventing external interference and accidental deadlocks.
    //private final Object Lock = new Object();

    private String myNickname;

    /**
     * Sets my nickname.
     *
     * @param myNickname the my nickname
     */
    public void setMyNickname(String myNickname) { this.myNickname = myNickname; }

    /**
     * Gets my nickname.
     *
     * @return the my nickname
     */
    public String getMyNickname() { return this.myNickname; }

    /**
     * Register observer.
     *
     * @param observer the observer
     */
// Registers an observer (CLIView or GUIView)
    // Called during client initialization
    public void registerObserver(ModelObserver observer) {
        // Add observer to the list (consider synchronization)

        // CopyOnWriteArrayList has the addIfAbsent method built-in!
        // It is thread-safe and does not need a synchronized block
        observers.addIfAbsent(observer);

    }

    /**
     * Update value.
     *
     * @param newState the new state
     */
// Updates the current state with the GameState received from the server
    // Notifies all registered observers
    // Called by RmiClient.updateView() or by the Socket reader thread
    public void updateValue(GameState newState) {
        // Update local state and notify observers (consider synchronization)
        this.currentState = newState;
        // No longer need to create a manual copy (new ArrayList)
        // No longer need synchronized(Lock)
        // CopyOnWriteArrayList guarantees the iterator is a safe "snapshot"
        for (ModelObserver observer : observers) {
            observer.onStateUpdate(newState);
        }
    }

    /**
     * Notify error.
     *
     * @param errorMessage the error message
     */
// Notifies observers about an error message
    // Called by RmiClient.signalError()
    public void notifyError(String errorMessage) {
        // Clean, fast, and thread-safe
        for (ModelObserver observer : observers) {
            observer.onError(errorMessage);
        }
    }

    /**
     * Notify abort.
     *
     * @param errorMessage the error message
     */
    public void notifyAbort(String errorMessage){
        for (ModelObserver observer : observers) {
            observer.onAbort(errorMessage);
        }
    }


// LOCAL VALIDATION — used by ClientController

    /**
     * Is my turn boolean.
     *
     * @param nickname the nickname
     * @return the boolean
     */
// Checks if it is the turn of the specified player
    // Used by ClientController before sending moveTotem
    public boolean isMyTurn(String nickname) {
        // Return true if the active player matches the nickname
        if (currentState == null || currentState.getActivePlayerNickname() == null) {
            return false;
        }
        return currentState.getActivePlayerNickname().equals(nickname);
    }

    /**
     * Is tile free boolean.
     *
     * @param offerTileId the offer tile id
     * @return the boolean
     */
// Checks if an OfferTile is free
    public boolean isTileFree(char offerTileId) {
        // Return true if the tile exists and is not occupied
        if (currentState == null || currentState.getOfferTileStates() == null) {
            return false;
        }

        return currentState.getOfferTileStates().stream()
                .filter(tile -> tile.getLetter() == offerTileId)
                .findFirst()
                .map(tile -> !tile.isOccupied())
                .orElse(false);
    }

    /**
     * Is current phase boolean.
     *
     * @param phaseName the phase name
     * @return the boolean
     */
// Checks if the current phase matches the given phase name
    public boolean isCurrentPhase(String phaseName) {
        // Return true if the phase matches the current state's phase
        if (currentState == null || currentState.getCurrentPhaseName() == null) {
            return false;
        }
        return currentState.getCurrentPhaseName().equals(phaseName);
    }

    /**
     * Gets current state.
     *
     * @return the current state
     */
// Returns the current GameState stored in the LocalModel
    public GameState getCurrentState() {
        // Return the cached GameState
        return currentState;
    }
}
