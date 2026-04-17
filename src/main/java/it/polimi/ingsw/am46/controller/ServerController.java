package it.polimi.ingsw.am46.controller;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.network.GameState;
import it.polimi.ingsw.am46.network.VirtualView;

/*
 Oracle Guardian. Receives commands from clients (via RmiServer or SocketServer),
 calls the Game, catches exceptions, and notifies clients via VirtualView.
 Knows nothing about RMI or Sockets — communicates only with VirtualView.
 All public methods are synchronized to prevent
 data races when multiple clients call simultaneously.
 */

public class ServerController {

    // Il Game — l'Oracolo, fonte di verità assoluta
    private final Game game;

    // VirtualView — il Broadcast Manager
    // Iniettata dopo la creazione (con RMI → RmiServer,
    // con Socket → SocketServer)
    private VirtualView virtualView;

    // Maybe we should add a parameter and a method for setting the
    // expected players (?),
    // private int expectedPlayers;
    //public void setExpectedPlayers(int n) {
        //this.expectedPlayers = n;
    //}

    public ServerController() {
        this.game = new Game();
    }

    public void setVirtualView(VirtualView virtualView) {
        this.virtualView = virtualView;
    }


// COMMANDS FROM CLIENTS


    /*
     * Handles a new client connection.
     * Adds the player to the Game, registers the CUR in the VirtualView,
     * and starts the game when all expected players are connected.
     */
    public synchronized void connect(String nickname, Object cur) {

    }

    /*
     * Handles a totem placement request.
     * Validates the action, calls the Game, and broadcasts the updated state.
     * Sends an error only to the requesting client if validation fails.
     */
    public synchronized void moveTotem(String nickname, String offerTileId) {

    }

    /*
     * Handles a request to take a card from the board.
     * Validates the action, updates the Game, and broadcasts the new state.
     */
    public synchronized void addCard(String nickname, String cardId) {

    }

    /*
     Handles the optional extra card draw.
     If cardId is null, the player intentionally skips the extra draw.
     */
    public synchronized void addExtraCard(String nickname, String cardId) {

    }


     //Shortcut for skipping the extra draw phase.

    public synchronized void skipExtraDraw(String nickname) {
        // Equivalent to addExtraCard(nickname, null)
    }

    /*
     * Handles a client disconnection detected by the RMI layer.
     * Removes the client from the VirtualView and notifies remaining players.
     */
    public synchronized void handleDisconnection(String nickname) {

    }

// PRIVATE UTILITIES

    /*
     Builds a serializable GameState snapshot from the current Game.
     This object is sent to clients through the network.
     */
    private GameState buildGameState() {
        // Create and return a new GameState snapshot
        return new GameState(game);
    }

    /*
     Retrieves a Player by nickname.
     Throws IllegalStateException if the player does not exist.
     */
    private Player getPlayerByNickname(String nickname) {
        // Search for the player in the Game
        return null; // placeholder
    }

    /**
     * Retrieves an OfferTile by its ID.
     * Throws IllegalStateException if the tile does not exist.
     */
    private OfferTile getOfferTileById(String offerTileId) {
        // Search for the tile in the board
        return null; // placeholder
    }

    /*
     Retrieves a Card by its ID from the board.
     Throws IllegalStateException if the card does not exist.
     */
    private Card getCardById(String cardId) {
        // Search for the card in topRow or bottomRow
        return null; // placeholder
    }

    /*
     Sends an error message to a specific client.
     If sending fails, the client is considered disconnected.
     */
    private void sendErrorToClient(String nickname, String message) {

    }






}
