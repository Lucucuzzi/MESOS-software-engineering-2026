package it.polimi.ingsw.am46.network;

import it.polimi.ingsw.am46.network.rmi.client.VirtualViewRmi;

public interface VirtualServer<T> {
    /*
     * Base interface that defines the commands the client
     * can send to the server. Independent of the network technology.
     * With RMI → VirtualServerRmi extends Remote, VirtualServer
     * With Socket → VirtualServerSocket extends VirtualServer
     */


    /**
     * The client connects to the game by passing its
     * nickname and a reference to its ClientUpdateReceiver.
     * The server registers it as an observer in the VirtualView.
     */
    void connect(String nickname, T cur) throws Exception;
    void setExpectedPlayers(String nickname, int numPlayers) throws Exception;
    // By using Generics T , we are telling the system : accept anything as a receiver
    // in this way, the interface is independent of the network technology.

    /*
     The client wants to place the totem on an Offer tile.
     The server validates: is it the right turn? Is the tile free?
     */
    void moveTotem(String nickname, String offerTileId) throws Exception;

    /*
     The client wants to take a card from the board.
     The server validates: turn, phase, food, card available.
     */
    void addCard(String nickname, String cardId) throws Exception;

    /*
     The client wants to draw the optional extra card
     (only if they have the special building).
     cardId == null means a voluntary skip.
     */
    void addExtraCard(String nickname, String cardId) throws Exception;


     //The client explicitly skips the ExtraDraw phase
    void skipExtraDraw(String nickname) throws Exception;

    void chooseColor(String nickname, String colorName) throws Exception;

}
