package it.polimi.ingsw.am46.controller;

/**
 * Oracle Guardian.
 * Receives commands from clients (via RmiServer or SocketServer),
 * calls the Game, catches exceptions, and notifies clients
 * via VirtualView.
 * Knows nothing about RMI or Sockets — communicates only with VirtualView.
 * All public methods are synchronized to prevent
 * data races when multiple clients call simultaneously.
 */

public class ServerController {


}
