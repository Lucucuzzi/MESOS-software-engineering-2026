package it.polimi.ingsw.am46.network.socket.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.server.controller.ServerController;
import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.server.model.Color;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Socket client handler.
 */
public class SocketClientHandler implements Runnable, NetworkMode {
    private final ServerController controller;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson = new Gson();

    private String nickname;
    private volatile boolean running = true;

    private volatile boolean loginComplete = false;
    private final List<String> messageQueue = new ArrayList<>();

    // Tracks the last time we heard from the client.
    // Updated on every pong to prevent false timeouts.
    private long lastPongTime = System.currentTimeMillis();

    /**
     * Instantiates a new Socket client handler.
     *
     * @param controller the controller
     * @param in         the in
     * @param out        the out
     */
    public SocketClientHandler(ServerController controller, BufferedReader in, PrintWriter out) {
        this.controller = controller;
        this.in = in;
        this.out = out;
    }
    @Override
    public void run() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                dispatch(line);
            }
        } catch (IOException e) {
            // Socket closed or network error
        } finally {
            if (nickname != null) controller.handleDisconnection(nickname);
        }
    }

    // INBOUND : CALLS SERVERCONTROLLER
    private void dispatch(String jsonLine) {
        try{
            JsonObject msg = JsonParser.parseString(jsonLine).getAsJsonObject();
            String type = msg.get("type").getAsString();
            switch (type) {
                case "getColors" -> {
                    // Prende i colori liberi dal controller (es. lista di Enum)
                    List<Color> availableColors = controller.getAvailableColors();

                    JsonArray colorsArray = new JsonArray();
                    for (Object c : availableColors) {
                        colorsArray.add(c.toString());
                    }

                    JsonObject res = new JsonObject();
                    res.addProperty("type", "availableColors");
                    res.addProperty("status", "OK");
                    res.add("colors", colorsArray);
                    out.println(gson.toJson(res));
                }
                case "connect"->{
                    String requestedNickname = msg.get("nickname").getAsString();
                    String requestedColor = msg.get("color").getAsString();
                    try {
                        controller.connect(requestedNickname,requestedColor, this);
                        this.nickname = requestedNickname;
                        // LOGIN OK
                        JsonObject okRes = new JsonObject();
                        okRes.addProperty("type", "connectCheck");
                        okRes.addProperty("status", "OK");
                        out.println(gson.toJson(okRes));
                        synchronized(this) {
                            loginComplete = true;
                            for (String queuedMsg : messageQueue) {
                                out.println(queuedMsg);
                            }
                            messageQueue.clear();
                        }
                    } catch (NicknameOfflineException e) {
                        this.nickname = requestedNickname;
                        JsonObject res = new JsonObject();
                        res.addProperty("type", "connectCheck");
                        res.addProperty("status", "OFFLINE");
                        out.println(gson.toJson(res));
                    }catch (GameAlreadyStartedException e) {
                        JsonObject errRes = new JsonObject();
                        errRes.addProperty("type", "connectCheck");
                        errRes.addProperty("status", "ALREADY_STARTED");
                        errRes.addProperty("message", e.getMessage());
                        out.println(gson.toJson(errRes));

                    } catch (InvalidConnectionException e) {
                        JsonObject errRes = new JsonObject();
                        errRes.addProperty("type", "connectCheck");
                        errRes.addProperty("status", "INVALID_DATA");
                        errRes.addProperty("message", e.getMessage());
                        out.println(gson.toJson(errRes));

                    } catch (Exception e) {
                        // ERRORI GENERICI
                        JsonObject errRes = new JsonObject();
                        errRes.addProperty("type", "connectCheck");
                        errRes.addProperty("status", "ERROR");
                        errRes.addProperty("message", e.getMessage());
                        out.println(gson.toJson(errRes));
                    }
                }

                case "moveTotem"->{
                    String nick = msg.get("nickname").getAsString();
                    String tileId = msg.get("offerTileId").getAsString();
                    controller.moveTotem(nick,tileId);
                }
                case "addCard"->{
                    String nick = msg.get("nickname").getAsString();
                    String cardId = msg.get("cardId").getAsString();
                    controller.addCard(nick,cardId);
                }
                case "extraDraw"->{
                    String nick = msg.get("nickname").getAsString();
                    String cardId = msg.get("cardId").isJsonNull() ? null : msg.get("cardId").getAsString();
                    controller.addExtraCard(nick,cardId);
                }
                case "skipExtra"->{
                    controller.skipExtraDraw(msg.get("nickname").getAsString());
                }
                case "pong"->{
                    this.lastPongTime = System.currentTimeMillis();
                }
                case "setExpectedPlayers"->{
                    String nick = msg.get("nickname").getAsString();
                    int expectedPlayers = msg.get("numPlayers").getAsInt();
                    controller.setExpectedPlayers(nick,expectedPlayers);
                }
                case "reconnect" -> {
                    String nick = msg.get("nickname").getAsString();
                    this.nickname = nick;
                    synchronized(this) {
                        loginComplete = true;
                        for (String queuedMsg : messageQueue) out.println(queuedMsg);
                        messageQueue.clear();
                    }
                    // controller.reconnect() aggiorna il modello, cancella il timer,
                    // e fa broadcastUpdate a tutti (incluso questo handler che è già attivo)
                    controller.reconnect(nick, this);
                    // Manda conferma esplicita: il client sa con certezza che è una reconnect
                    JsonObject res = new JsonObject();
                    res.addProperty("type", "reconnectConfirm");
                    out.println(gson.toJson(res));
                }

                case "getLeaderboard" -> {
                    int numPlayers = msg.get("numPlayers").getAsInt();
                    List<LeaderboardEntry> leaderboard = controller.getLeaderboard(numPlayers);

                    // Serializza la risposta in JSON
                    JsonObject response = new JsonObject();
                    JsonArray entries = new JsonArray();
                    for (LeaderboardEntry entry : leaderboard) {
                        JsonObject e = new JsonObject();
                        e.addProperty("nickname", entry.getNickname());
                        e.addProperty("totalWins", entry.getTotalWins());
                        entries.add(e);
                    }
                    response.add("leaderboard", entries);
                    out.println(gson.toJson(response));
                }

                case "getPlayerPosition" -> {
                    String nickname = msg.get("nickname").getAsString();
                    int numPlayers  = msg.get("numPlayers").getAsInt();
                    int position    = controller.getPlayerPosition(nickname, numPlayers);

                    JsonObject response = new JsonObject();
                    response.addProperty("position", position);
                    out.println(gson.toJson(response));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to dispatch message: " + e.getMessage());
            e.printStackTrace();
            sendError("Server error processing your request: " + e.getMessage());
        }
    }


    /**
     * Send update.
     *
     * @param gameState the game state
     */
// OUTBOUND : TO THE CLIENT, CALLED BY SOCKETSERVER
    public void sendUpdate(GameState gameState) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "update");
        msg.add("gameState", gson.toJsonTree(gameState));
        String jsonToSend = gson.toJson(msg);
        if (!loginComplete) { // we can't send an update if the client is not logged
            // if the update arrives before the message ConnectionCheck, the client doesn't see
            // the field 'STATUS' -> NULLPOINTEREXCEPTION
            messageQueue.add(jsonToSend);
        } else {
            out.println(jsonToSend);
        }
    }

    /**
     * Send error.
     *
     * @param errorMessage the error message
     */
    public void sendError(String errorMessage) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "error");
        msg.addProperty("message", errorMessage);
        out.println(gson.toJson(msg));
    }

    /**
     * Send winner.
     *
     * @param finalState the final state
     */
    public void sendWinner(GameState finalState){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "winner");
        msg.add("gameState",gson.toJsonTree(finalState));
        out.println(gson.toJson(msg));
    }

    /**
     * Send ping.
     */
    public void sendPing(){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "ping");
        out.println(gson.toJson(msg));
    }

    /**
     * Stop running.
     */
    public void stopRunning() {
        running = false;
    }

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean isSocket() throws RemoteException {
        return true;
    }

    /**
     * Send abort.
     *
     * @param message the message
     */
    public void sendAbort(String message){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "abort");
        msg.addProperty("message", message);
        out.println(gson.toJson(msg));
    }

    /**
     * Gets last pong time.
     *
     * @return the last pong time
     */
    public long getLastPongTime() {
        return lastPongTime;
    }

}
