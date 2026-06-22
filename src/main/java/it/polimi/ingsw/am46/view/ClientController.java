package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.VirtualServer;
import it.polimi.ingsw.am46.network.async.CommandQueue;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;


/*
 * Client-side controller.
 * Receives events from the view, performs local validation
 * on the LocalModel, and sends commands to the server via VirtualServer.
 * It doesn't know whether RMI or sockets are being used underneath—it only communicates with VirtualServer.
 */

/**
 * The type Client controller.
 */
public class ClientController {
    // Reference to the VirtualServer (RMI or Socket)
    private VirtualServer server;
    // LocalModel containing the latest GameState
    private final LocalModel localModel;
    // Nickname of this client
    private String myNickname;
    private final CommandQueue commandQueue;

    private boolean reconnecting = false;

    private Object myNetworkReference; // Can be RmiClient or SocketClientProxy

    /**
     * Sets my network reference.
     *
     * @param ref the ref
     */
    public void setMyNetworkReference(Object ref) {
        this.myNetworkReference = ref;
    }


    /**
     * Instantiates a new Client controller.
     *
     * @param localModel the local model
     * @param onError    the on error
     */
    public ClientController(LocalModel localModel, java.util.function.Consumer<String> onError) {
        // CommandQueue lives here — one queue per client session
        this.commandQueue = new CommandQueue(onError);
        this.localModel = localModel;
    }

    /**
     * Instantiates a new Client controller.
     *
     * @param localModel the local model
     */
// Convenience constructor that routes errors through LocalModel
    public ClientController(LocalModel localModel) {
        // Delegate to main constructor using LocalModel's notifyError as handler
        this(localModel, localModel::notifyError);
    }

    /**
     * Sets reconnecting.
     *
     * @param r the r
     */
    public void setReconnecting(boolean r) {
        this.reconnecting = r;
    }

    /**
     * Is reconnecting boolean.
     *
     * @return the boolean
     */
    public boolean isReconnecting() {
        return reconnecting;
    }

    /**
     * Sets server.
     *
     * @param server the server
     */
// Sets the server endpoint (stub)
    public void setServer(VirtualServer<?> server) {
        this.server = server;
    }

    /**
     * Sets nickname.
     *
     * @param nickname the nickname
     */
// Sets the player's nickname
    public void setNickname(String nickname) {
        this.myNickname = nickname;
    }

    /**
     * Gets my nickname.
     *
     * @return the my nickname
     */
    public String getMyNickname() {
        return myNickname;
    }

// =========================================================
// EVENTS FROM THE VIEW
// =========================================================

    /**
     * On set expected players.
     *
     * @param numPlayers the num players
     */
    public void onSetExpectedPlayers(int numPlayers) {
        //any controls to put

        commandQueue.submit(() -> {
            try {
                server.setExpectedPlayers(myNickname, numPlayers);
            } catch (Exception e) {
                // Custom message as required
                localModel.notifyError("Unable to send the selected player count");
            }
        });
    }

    /**
     * On reconnect.
     *
     * @param nickname the nickname
     */
    public void onReconnect(String nickname) {
        commandQueue.submit(() -> {
            try {
                server.reconnect(nickname, myNetworkReference);
            } catch (Exception e) {
                localModel.notifyError(
                        "Reconnection failed: "+ e.getMessage());
            }
        });

    }


    /**
     * On move totem boolean.
     *
     * @param offerTileId the offer tile id
     * @return the boolean
     */
// Called when the user wants to place the totem on a tile
    public boolean onMoveTotem(String offerTileId) {
        // Check if it's the player's turn
        // If not, notify error locally and stop
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!");
            return false;
        }

        // Check if the tile is free
        // If not, notify error locally and stop
        char letter = offerTileId.charAt(0);
        if (!localModel.isTileFree(letter)) {
            localModel.notifyError("Tile " + offerTileId + " is already occupied or does not exist.");
            return false;
        }

        // Check if the current phase is PlaceTotemState
        // If not, notify error locally and stop
        if (!localModel.isCurrentPhase("PlaceTotemState")) {
            localModel.notifyError("You cannot place the totem in this phase of the game.");
            return false;
        }

        // Send the moveTotem command to the server
        // If the network fails, notify error locally
        commandQueue.submit(() -> {
            try {
                // Calling the server using your myNickname attribute
                server.moveTotem(myNickname, offerTileId);
            } catch (Exception e) {
                // Error handling with the exact text you requested
                localModel.notifyError("Network error while sending the move totem command.");
            }
        });
        return true;
    }

    /**
     * On add card boolean.
     *
     * @param cardId the card id
     * @return the boolean
     */
// Called when the user wants to take a card
    public boolean onAddCard(String cardId) {
        // Check if the current phase is AddCardState
        // If not, notify error locally and stop
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!");
            return false;
        }

        if (!localModel.isCurrentPhase("AddCardState")) {
            localModel.notifyError("You cannot take a card in this phase of the game.");
            return false;
        }

        // validation for food cost in the server

        // Send the addCard command to the queue
        // If the network fails, notify error locally
        commandQueue.submit(() -> {
            try {
                // Calling the server using the myNickname class attribute
                server.addCard(myNickname, cardId);
            } catch (Exception e) {
                // Immediate notification to the LocalModel with the requested text
                localModel.notifyError("Network error while trying to add the card.");

            }
        });
        return true;
    }


    /**
     * On add extra card boolean.
     *
     * @param cardId the card id
     * @return the boolean
     */
// Called when the user wants to take the extra card
    // If cardId is null, the user is skipping the extra draw
    public boolean onAddExtraCard(String cardId) {
        // Send the addExtraCard command to the server
        // If the network fails, notify error locally
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!!");
            return false;
        }
        commandQueue.submit(() -> {
            try {
                // Calling the server with your myNickname attribute
                server.addExtraCard(myNickname, cardId);
            } catch (Exception e) {
                // Notify the localModel with the exact text requested
                localModel.notifyError("Network error while trying to add the extra card.");

            }
        });
        return true;
    }


    /**
     * On skip extra draw boolean.
     *
     * @return the boolean
     */
// Called when the user wants to skip the ExtraDraw phase
    public boolean onSkipExtraDraw() {
        // Send the skipExtraDraw command to the server
        // If the network fails, notify error locally
        if (!localModel.isMyTurn(myNickname)) {
            localModel.notifyError("It's not your turn!");
            return false;
        }

        commandQueue.submit(() -> {
            try {
                // Calling the server with your myNickname attribute
                server.skipExtraDraw(myNickname);
            } catch (Exception e) {
                // Specific error message as required
                localModel.notifyError("Network error while trying to skip the extra draw.");

            }
        });
        return true;
    }

    /**
     * Gets leaderboard.
     *
     * @param numPlayers the num players
     * @return the leaderboard
     */
// ADD the two ranking methods
    public List<LeaderboardEntry> getLeaderboard(int numPlayers) {
        try {
            return server.getLeaderboard(numPlayers);
        } catch (Exception e) {
            localModel.notifyError("Network error while fetching leaderboard.");
            return new ArrayList<>();
        }
    }

    /**
     * Gets player position.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     * @return the player position
     */
    public int getPlayerPosition(String nickname, int numPlayers) {
        try {
            return server.getPlayerPosition(nickname, numPlayers);
        } catch (Exception e) {
            localModel.notifyError("Network error while fetching player position.");
            return -1;
        }
    }

}
