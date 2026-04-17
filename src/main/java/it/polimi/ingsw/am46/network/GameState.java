package it.polimi.ingsw.am46.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.Card;



/**
 * Data Transfer Object — a snapshot of the game's state.
 * It must be serializable to be transmitted over the network via RMI.
 * It contains only raw data (no logic, no references
 * to the actual game) — it is an immutable snapshot of the state.
 * The LocalModel on the client is updated with this object.
 * The View reads from this object to update itself.
 */

public class GameState implements Serializable {
    // Java serialization version
    private static final long serialVersionUID = 1L;

    // General game state
    /*private final int round;
    private final int currentEra;
    private final String currentPhaseName;
    private final String activePlayerNickname;
    private final boolean isGameOver;

     */

    // Board state
    // We use card IDs to avoid serializing complex objects
    /*private final List<Integer> topRowCardIds;
    private final List<Integer> bottomRowCardIds;

    // Player states
    private final List<PlayerState> playerStates;

    // Offer tile states
    private final List<OfferTileState> offerTileStates;

    // Winners (populated only at the end of the game)
    private final List<String> winners;

    // Context message (e.g., “New Era!”, “Round 5”)
    private final String contextMessage;

     */

    public GameState(Game game) {
        // Constructs the DTO by reading data from the Game

        // Populate the board rows with card IDs

        // Populate the player states


        // Populate the Offer tiles


    }


}
