package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.dto.GameState;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.ArrayList;
import java.util.List;

/*
 * Client-side local cache of the game state.
 * It is updated ONLY when a GameState is received from the server.
 * It does not contain any game logic—it is merely a snapshot.
 * Implements the Observer Pattern:
 * maintains a list of ModelObservers (the Views)
 * and notifies them when the state changes.
 */



public class LocalModel {

    //The current GameState received from the server
    private GameState currentState;

    // List of observers (Views) registered on the client
    // Utilizziamo CopyOnWriteArrayList al posto di una ArrayList standard con Lock/Synchronized per due motivi principali:
// 1. ELIMINAZIONE DELLE CONCURRENT-MODIFICATION: Il pattern "Copy-On-Write" garantisce che ogni iteratore
//    lavori su uno snapshot della lista. Questo evita crash (ConcurrentModificationException) se un
//    observer si registra o si disconnette proprio mentre il sistema sta ciclando sulla lista per inviare
//    un aggiornamento di stato.
// 2. PERFORMANCE NELLE LETTURE: In questa applicazione, le letture (notifiche agli observer) sono estremamente
//    frequenti, mentre le scritture (registrazione observer) avvengono quasi solo nella fase iniziale.
//    Rimuovendo i blocchi 'synchronized', permettiamo a più thread (RMI, Socket e UI) di accedere alla lista
//    contemporaneamente senza rallentamenti o colli di bottiglia.
    private final CopyOnWriteArrayList<ModelObserver> observers = new CopyOnWriteArrayList<>();

    // Aggiungi il campo in cima alla classe insieme agli altri
    private volatile boolean reconnectConfirmed = false;

    // Aggiungi questi due metodi
    public void notifyReconnectConfirmed() {
        this.reconnectConfirmed = true;
    }

    public boolean isReconnectConfirmed() {
        return reconnectConfirmed;
    }

    // Best practice: A private lock object encapsulates synchronization,
    // preventing external interference and accidental deadlocks.
    //private final Object Lock = new Object();

    private String myNickname;
    public void setMyNickname(String myNickname) { this.myNickname = myNickname; }
    public String getMyNickname() { return this.myNickname; }

    // Registers an observer (CLIView or GUIView)
    // Called during client initialization
    public void registerObserver(ModelObserver observer) {
        // Add observer to the list (consider synchronization)

        // CopyOnWriteArrayList ha il metodo addIfAbsent incluso!
        // È thread-safe e non serve il blocco synchronized
        observers.addIfAbsent(observer);

    }

    // Updates the current state with the GameState received from the server
    // Notifies all registered observers
    // Called by RmiClient.updateView() or by the Socket reader thread
    public void updateValue(GameState newState) {
        // Update local state and notify observers (consider synchronization)
        this.currentState = newState;
        // Non serve più creare una copia manuale (new ArrayList)
        // Non serve più il synchronized(Lock)
        // CopyOnWriteArrayList garantisce che l'iteratore sia una "istantanea" sicura
        for (ModelObserver observer : observers) {
            observer.onStateUpdate(newState);
        }
    }

    // Notifies observers about an error message
    // Called by RmiClient.signalError()
    public void notifyError(String errorMessage) {
        // Pulito, veloce e thread-safe
        for (ModelObserver observer : observers) {
            observer.onError(errorMessage);
        }
    }
    public void notifyAbort(String errorMessage){
        for (ModelObserver observer : observers) {
            observer.onAbort(errorMessage);
        }
    }


// LOCAL VALIDATION — used by ClientController

    // Checks if it is the turn of the specified player
    // Used by ClientController before sending moveTotem
    public boolean isMyTurn(String nickname) {
        // Return true if the active player matches the nickname
        if (currentState == null || currentState.getActivePlayerNickname() == null) {
            return false;
        }
        return currentState.getActivePlayerNickname().equals(nickname);
    }

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

    // Checks if the current phase matches the given phase name
    public boolean isCurrentPhase(String phaseName) {
        // Return true if the phase matches the current state's phase
        if (currentState == null || currentState.getCurrentPhaseName() == null) {
            return false;
        }
        return currentState.getCurrentPhaseName().equals(phaseName);
    }

    // Returns the current GameState stored in the LocalModel
    public GameState getCurrentState() {
        // Return the cached GameState
        return currentState;
    }
}
