package it.polimi.ingsw.am46.network;

public interface VirtualView {
    /*
     * Base interface that defines the notifications the server
     * can send to clients. Independent of network technology.
     * Implemented by the server-side RMI or Socket layer.
     * This is the Broadcast Manager — it manages the list of registered CURs.
     */
}
