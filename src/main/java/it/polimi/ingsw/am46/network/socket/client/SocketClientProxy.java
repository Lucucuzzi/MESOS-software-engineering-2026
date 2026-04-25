package it.polimi.ingsw.am46.network.socket.client;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.polimi.ingsw.am46.network.VirtualServer;

import java.io.BufferedWriter;
import java.io.PrintWriter;

public class SocketClientProxy implements VirtualServer<Void> {
    private final PrintWriter out;
    private final Gson gson = new Gson();

    public SocketClientProxy(BufferedWriter writer) {
        this.out = new PrintWriter(writer, true);
    }
    @Override
    public void connect(String nickname, Void cur) {
        // socket doesn't need cur, we use TCP, not a stub
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "connect");
        msg.addProperty("nickname", nickname);
        out.println(gson.toJson(msg));
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
