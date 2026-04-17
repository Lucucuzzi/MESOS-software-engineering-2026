package it.polimi.ingsw.am46.network;

import java.io.Serializable;

/**
 * Data Transfer Object — a snapshot of the game's state.
 * It must be serializable to be transmitted over the network via RMI.
 * It contains only raw data (no logic, no references
 * to the actual game) — it is an immutable snapshot of the state.
 * The LocalModel on the client is updated with this object.
 * The View reads from this object to update itself.
 */

public class GameState implements Serializable {
}
