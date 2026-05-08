package it.polimi.ingsw.am46.network.socket.client;

import com.google.gson.*;
import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.network.VirtualServer;

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

}
