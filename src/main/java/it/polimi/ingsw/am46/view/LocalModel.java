package it.polimi.ingsw.am46.view;

/**
 * Client-side local cache of the game state.
 * It is updated ONLY when a GameState is received from the server.
 * It does not contain any game logic—it is merely a snapshot.
 * Implements the Observer Pattern:
 * maintains a list of ModelObservers (the Views)
 * and notifies them when the state changes.
 */

public class LocalModel {
}
