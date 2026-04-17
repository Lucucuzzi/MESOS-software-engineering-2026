package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.dto.GameState;

/**
 * Interfaccia Observer per il pattern LocalModel → View.
 * Implementata da CLIView e GUIView.
 */

public interface ModelObserver {

    /*
     Chiamato da LocalModel quando arriva un aggiornamento
     dal server. La View si aggiorna con il nuovo stato.
     */
    void onStateUpdate(GameState newState);


     //Chiamato da LocalModel quando arriva un errore.

    void onError(String errorMessage);
}
