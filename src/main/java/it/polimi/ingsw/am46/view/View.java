package it.polimi.ingsw.am46.view;

/*
 Text-based interface (Command Line Interface).
 Implements ModelObserver → receives notifications from LocalModel.
 Displays the game state in text format.
 */

import it.polimi.ingsw.am46.network.dto.GameState;

public class View implements ModelObserver{

    @Override
    public void onStateUpdate(GameState newState) {
        // Update the visual representation using the new GameState
        // Display general game information
    }

    @Override
    public void onError(String errorMessage) {
        // Display an error message to the user
    }

}
