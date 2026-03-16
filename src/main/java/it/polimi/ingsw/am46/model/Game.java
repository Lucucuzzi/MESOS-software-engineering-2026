package it.polimi.ingsw.am46.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Player activePlayer;
    private int round;
    private int currentEra;
    private ArrayList<Color> availableColors;
    // private int pp e food, da ipotizzare infinita

    public Game(){
        this.activePlayer = null;
        this.round = 1;
        this.currentEra = 1;
        this.availableColors = new ArrayList<>(List.of(Color.values()));
    }

    public Player getActivePlayer() {
        return activePlayer;
    }
    public int  getRound() {
        return round;
    }
    public int getCurrentEra() {
        return currentEra;
    }
    public ArrayList<Color> getAvailableColors() {
        return availableColors;
    }

    public void setActivePlayer(ArrayList<Player> activePlayers) {
        //da implementare in base a logica dei turni


    }

    public void setUpGame(int numOfPlayers){
        //da implementare
        //chiamerà in fila tutte le setUp di Board
    }

}
