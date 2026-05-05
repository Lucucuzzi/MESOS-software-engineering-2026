package it.polimi.ingsw.am46.network.socket.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.LocalModel;

import java.io.BufferedReader;
import java.io.IOException;


public class SocketListener implements Runnable {
    private final BufferedReader in;
    private final LocalModel localModel;
    private final SocketClientProxy serverProxy;
    private final Gson gson = new Gson();
    private volatile boolean running = true;

    public SocketListener(BufferedReader in, LocalModel localModel, SocketClientProxy serverProxy) {
        this.in = in;
        this.localModel = localModel;
        this.serverProxy = serverProxy;
    }

    @Override
    public void run() {
        try{
            String line;
            while(running && ((line = in.readLine())!=null)){
                handle(line);
            }
            if (running){
                localModel.notifyError("Lost connection to server");
            }
        } catch(IOException e){
            if(running){
                localModel.notifyError("Connection error: "+e.getMessage());
            }
        }
    }

    // Deserializes JSON and update LocalModel.
    // LocalModel notify View through Observer Pattern.
    private void handle(String jsonLine) {
        try {
            JsonObject msg = JsonParser.parseString(jsonLine).getAsJsonObject();
            String type = msg.get("type").getAsString();

            switch (type) {
                case "update" -> {
                    GameState state = gson.fromJson(msg.get("gameState"), GameState.class);
                    localModel.updateValue(state);
                }
                case "error" -> {
                    String errorMsg = msg.get("message").getAsString();
                    localModel.notifyError(errorMsg);
                }
                case "winner" -> {
                    GameState finalState = gson.fromJson(msg.get("gameState"), GameState.class);
                    localModel.updateValue(finalState);
                    stop(); // game is finished, stop running
                }
                case "abort" -> {
                    localModel.notifyAbort("Game aborted: " + msg.get("message").getAsString());
                    stop(); // game is finished, stop running
                }
                case "ping" -> {
                    serverProxy.sendPong();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to handle message: " + e.getMessage());
        }
    }

    public void stop() { running = false; }

}
