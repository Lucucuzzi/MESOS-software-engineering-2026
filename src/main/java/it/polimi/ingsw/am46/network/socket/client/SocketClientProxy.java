package it.polimi.ingsw.am46.network.socket.client;

import com.google.gson.*;
import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.exception.NicknameOfflineException;
import it.polimi.ingsw.am46.network.VirtualServer;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SocketClientProxy implements VirtualServer<Void> {
    private final PrintWriter out;
    private final BufferedReader in; // <-- we need it for showing available colors
    private final Gson gson = new Gson();


    public SocketClientProxy(BufferedWriter writer, BufferedReader reader) {
        this.out = new PrintWriter(writer, true);
        this.in = reader;
    }

    @Override
    public void connect(String nickname, String colorName, Void cur) throws Exception{
        // socket doesn't need cur, we use TCP, not a stub
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "connect");
        msg.addProperty("nickname", nickname);
        msg.addProperty("color", colorName);
        out.println(gson.toJson(msg));

        String responseLine = in.readLine();
        JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();

        // IF SERVER (SocketClientHandler) SEND "ERROR", CLIENT THROWS EXCEPTION
        String status = response.get("status").getAsString();
        String errorMsg = response.has("message") ? response.get("message").getAsString() : "Errore sconosciuto";

        if ("ALREADY_STARTED".equals(status)) {
            throw new GameAlreadyStartedException(errorMsg);

        } else if ("INVALID_DATA".equals(status)) {
            throw new InvalidConnectionException(errorMsg);

        } else if ("OFFLINE".equals(status)) {
            // L'eccezione viene costruita qui lato client, non viaggia via rete.
            // Il ClientLauncher la cattura e imposta isReconnecting = true.
            throw new NicknameOfflineException(nickname);
        } else if ("ERROR".equals(status)) {
            throw new Exception(errorMsg);
        }
    }

    public List<String> getAvailableColors() throws Exception {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getColors");
        out.println(gson.toJson(msg));

        String responseLine = in.readLine();
        JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();

        List<String> colors = new ArrayList<>();
        JsonArray colorsArray = response.getAsJsonArray("colors");
        for (int i = 0; i < colorsArray.size(); i++) {
            colors.add(colorsArray.get(i).getAsString());
        }
        return colors;
    }
    @Override
    public void moveTotem(String nickname, String offerTileId){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "moveTotem");
        msg.addProperty("nickname", nickname);
        msg.addProperty("offerTileId", offerTileId);
        out.println(gson.toJson(msg));
    }
    @Override
    public void addCard(String nickname, String cardId){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "addCard");
        msg.addProperty("nickname", nickname);
        msg.addProperty("cardId", cardId);
        out.println(gson.toJson(msg));
    }
    @Override
    public void addExtraCard(String nickname, String cardId){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "extraDraw");
        msg.addProperty("nickname", nickname);
        if(cardId != null){
            msg.addProperty("cardId", cardId);
        }else{
            msg.add("cardId", JsonNull.INSTANCE);
        }
        out.println(gson.toJson(msg));
    }
    @Override
    public void skipExtraDraw(String nickname){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "skipExtra");
        msg.addProperty("nickname", nickname);
        out.println(gson.toJson(msg));
    }
    public void sendPong(){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "pong");
        out.println(gson.toJson(msg));
    }
    @Override
    public void setExpectedPlayers(String nickname, int numPlayers){
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "setExpectedPlayers");
        msg.addProperty("nickname", nickname);
        msg.addProperty("numPlayers", numPlayers);
        out.println(gson.toJson(msg));
    }

    /*
     * Sends a reconnect request to the server.
     * Used instead of connect() when the player was previously in a game and
     * got disconnected. The server will swap the old (dead) socket handler
     * with the new one and broadcast the current game state.
     * Socket-side, the new socket connection carries its own SocketClientHandler
     * as the CUR — the server does NOT need a separate stub (unlike RMI).
     * We just send the nickname so the server can find the existing Player in the model.
     * @param nickname the player's original nickname (must match model exactly)
     */
    @Override
    public void reconnect(String nickname, Void cur) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "reconnect");
        msg.addProperty("nickname", nickname);
        out.println(gson.toJson(msg));
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int numPlayers) throws Exception {
        // Manda la richiesta al server
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getLeaderboard");
        msg.addProperty("numPlayers", numPlayers);
        out.println(gson.toJson(msg));

        // Aspetta la risposta
        String responseLine = in.readLine();
        JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();

        // Deserializza la lista di LeaderboardEntry
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        JsonArray entries = response.getAsJsonArray("leaderboard");
        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            leaderboard.add(new LeaderboardEntry(
                    entry.get("nickname").getAsString(),
                    entry.get("totalWins").getAsInt()
            ));
        }
        return leaderboard;
    }

    @Override
    public int getPlayerPosition(String nickname, int numPlayers) throws Exception {
        // Manda la richiesta al server
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getPlayerPosition");
        msg.addProperty("nickname", nickname);
        msg.addProperty("numPlayers", numPlayers);
        out.println(gson.toJson(msg));

        // Aspetta la risposta
        String responseLine = in.readLine();
        JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();
        return response.get("position").getAsInt();
    }
}
