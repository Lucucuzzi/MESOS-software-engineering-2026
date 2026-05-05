package it.polimi.ingsw.am46.network.socket.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.am46.controller.ServerController;
import it.polimi.ingsw.am46.model.Color;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.dto.GameState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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
                    } catch (Exception e) {
                        // LOGIN FAILED
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
            }
        } catch (Exception e) {
            System.err.println("Failed to dispatch message: " + e.getMessage());
            e.printStackTrace();
            sendError("Server error processing your request: " + e.getMessage());
        }
    }


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

    public void sendError(String errorMessage) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "error");
        msg.addProperty("message", errorMessage);
        out.println(gson.toJson(msg));
    }
    public void sendWinner(GameState finalState){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "winner");
        msg.add("gameState",gson.toJsonTree(finalState));
        out.println(gson.toJson(msg));
    }
    public void sendPing(){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "ping");
        out.println(gson.toJson(msg));
    }
    public void stopRunning() {
        running = false;
    }
    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean isSocket() throws RemoteException {
        return true;
    }

    public long getLastPongTime() {
        return lastPongTime;
    }

}
