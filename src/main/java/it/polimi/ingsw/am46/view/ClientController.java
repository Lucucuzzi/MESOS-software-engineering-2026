package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.network.VirtualServer;
import it.polimi.ingsw.am46.network.async.CommandQueue;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;
import it.polimi.ingsw.am46.network.rmi.server.VirtualServerRmi;
import it.polimi.ingsw.am46.network.socket.client.SocketClientProxy;
import it.polimi.ingsw.am46.network.NetworkMode;

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

    private Object myNetworkReference; // Può essere RmiClient o SocketClientProxy

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
        // Delega al costruttore principale usando notifyError del LocalModel come handler
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
        //eventuali controlli da mettere

        commandQueue.submit(() -> {
            try {
                server.setExpectedPlayers(myNickname, numPlayers);
            } catch (Exception e) {
                // Messaggio personalizzato come richiesto
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
                        "Riconnessione fallita: " + e.getMessage());
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
                // Chiamata al server usando il tuo attributo myNickname
                server.moveTotem(myNickname, offerTileId);
            } catch (Exception e) {
                // Gestione dell'errore con il testo esatto che hai richiesto
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
                // Chiamata al server usando l'attributo della classe myNickname
                server.addCard(myNickname, cardId);
            } catch (Exception e) {
                // Notifica immediata al LocalModel con il testo richiesto
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
                // Chiamata al server con il tuo attributo myNickname
                server.addExtraCard(myNickname, cardId);
            } catch (Exception e) {
                // Notifica al localModel con il testo esatto richiesto
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
                // Chiamata al server con il tuo attributo myNickname
                server.skipExtraDraw(myNickname);
            } catch (Exception e) {
                // Messaggio di errore specifico come richiesto
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
// AGGIUNGI i due metodi per la classifica
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
