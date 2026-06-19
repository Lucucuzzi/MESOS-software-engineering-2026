package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.dto.GameState;

/**
 * Observer interface for the LocalModel → View pattern.
 * Implemented by CLIView and GUIView.
 */
public interface ModelObserver {
    /**
     * On state update.
     *
     * @param newState the new state
     */
/*
     Called by LocalModel when an update arrives
     from the server. The View updates with the new status.
     */
    void onStateUpdate(GameState newState);

    /**
     * On error.
     *
     * @param errorMessage the error message
     */
//Called by LocalModel when an error comes.
    void onError(String errorMessage);

    /**
     * On abort.
     *
     * @param errorMessage the error message
     */
    void onAbort(String errorMessage);
}
