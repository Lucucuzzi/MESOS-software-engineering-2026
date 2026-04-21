package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.VirtualServer;

/**
 * Client-side controller.
 * Receives events from the view, performs local validation
 * on the LocalModel, and sends commands to the server via VirtualServer.
 * It doesn't know whether RMI or sockets are being used underneath—it only communicates with VirtualServer.
 */

public class ClientController {
    // Reference to the VirtualServer (RMI or Socket)
    private VirtualServer server;
    // LocalModel containing the latest GameState
    private final LocalModel localModel;
    // Nickname of this client
    private String myNickname;

    // Constructor: stores the LocalModel reference
    public ClientController(LocalModel localModel) {
        this.localModel = localModel;
    }

    // Sets the server endpoint (stub)
    public void setServer(VirtualServer<?> server) {
        this.server = server;
    }

    // Sets the player's nickname
    public void setNickname(String nickname) {
        this.myNickname = nickname;
    }

// =========================================================
// EVENTS FROM THE VIEW
// =========================================================

    public void onSetExpectedPlayers(int numPlayers) {
        try {
            server.setExpectedPlayers(myNickname, numPlayers);
        } catch (Exception e) {
            localModel.notifyError("Unable to send the selected player count");
        }
    }

    // Called when the user wants to place the totem on a tile
    public void onMoveTotem(String offerTileId) {
        // Check if it's the player's turn
        // If not, notify error locally and stop
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("Non è il tuo turno!");
            return;
        }

        // Check if the tile is free
        // If not, notify error locally and stop
        char letter = offerTileId.charAt(0);
        if (!localModel.isTileFree(letter)) {
            localModel.notifyError("Tile " + offerTileId + " is already occupied or does not exist.");
            return;
        }

        // Check if the current phase is PlaceTotemState
        // If not, notify error locally and stop
        if (!localModel.isCurrentPhase("PlaceTotemState")) {
            localModel.notifyError("You cannot place the totem in this phase of the game.");
            return;
        }

        // Send the moveTotem command to the server
        // If the network fails, notify error locally
        try {
            server.moveTotem(myNickname, offerTileId);
        } catch (Exception e) {
            localModel.notifyError("Network error while sending the move totem command.");
        }
    }

    // Called when the user wants to take a card
    public void onAddCard(String cardId) {
        // Check if the current phase is AddCardState
        // If not, notify error locally and stop
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!!");
            return;
        }

        if (!localModel.isCurrentPhase("AddCardState")) {
            localModel.notifyError("You cannot take a card in this phase of the game.");
            return;
        }

        // validation for food cost in the server

        // Send the addCard command to the server
        // If the network fails, notify error locally
        try {
            server.addCard(myNickname, cardId);
        } catch (Exception e) {
            localModel.notifyError("Network error while trying to add the card.");
        }
    }


    // Called when the user wants to take the extra card
    // If cardId is null, the user is skipping the extra draw
    public void onAddExtraCard(String cardId) {
        // Send the addExtraCard command to the server
        // If the network fails, notify error locally
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!!");
            return;
        }
        try {
            server.addExtraCard(myNickname, cardId);
        } catch (Exception e) {
            localModel.notifyError("Network error while trying to add the extra card.");
        }
    }


    // Called when the user wants to skip the ExtraDraw phase
    public void onSkipExtraDraw() {
        // Send the skipExtraDraw command to the server
        // If the network fails, notify error locally
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!");
            return;
        }

        try {
            server.skipExtraDraw(myNickname);
        } catch (Exception e) {
            localModel.notifyError("Network error while trying to skip the extra draw.");
        }
    }
}
