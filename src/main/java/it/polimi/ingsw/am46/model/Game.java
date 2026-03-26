package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Player activePlayer;
    private final int round;
    private final int currentEra;
    private final ArrayList<Color> availableColors;
    private final Board board;
    private final ArrayList<Player> players;

    // private int pp e food, da ipotizzare infinita

    public Game(){
        this.activePlayer = null;
        this.round = 1;
        this.currentEra = 1;
        this.availableColors = new ArrayList<>(List.of(Color.values()));
        this.players = new ArrayList<>();  // lista vuota, si riempie durante setup
        this.board = new Board();          // inizializza il tabellone
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
    public Board getBoard() {
        return board;
    }
    public ArrayList<Player> getPlayers() {
        return players;
    }



    private void setActivePlayer(ArrayList<Player> activePlayers) {
        //da implementare in base a logica dei turni

    }

    public void setUpGame(int numOfPlayers){
        //da implementare
        //chiamerà in fila tutte le setUp di Board
    }

    // Risolve gli eventi presenti nella fila inferiore
    public void resolveEvents() {
        // logica da implementare
    }

    // Rimuove il colore scelto dai colori disponibili e modifica l'attributo privato
    public void updateAvailableColors(Color color) {
        availableColors.remove(color);
    }

    // Assegna una quantità di cibo a un giocatore dalla riserva comune
    public void assignFood(Player player, int quantity) {
        player.modifyFood(quantity);
    }

    // Assegna una quantità di PP a un giocatore dalla riserva comune
    public void assignPP(Player player, int quantity) {
        player.modifyPP(quantity);
    }

    // Gestisce la fine della partita
    public void endGame() {
        // logica da implementare
        //chiamerà i metodi privati di fine game
    }


    // Cambia l'era corrente e aggiorna le carte disponibili
    public void changeEra() {
        // logica da implementare
        //chiamera i metodi di board per il cambiamento dell'era
    }

    // Controlla che il giocatore possa muovere il totem sull'offerTile indicata
    public void moveTotem(Player player, OfferTile offerTile) {
        // logica da implementare: controlla isActivePlayer e chiama moveTotem privato
    }

    // Implementa la logica effettiva del movimento del totem
    private void moveTotem(OfferTile offerTile) {
        // logica da implementare
    }

    // Risolve le fasi di fine round in base alla offerTile
    //chiamera anche RemoveFromBoard
    public void resolveRound() {
        // logica da implementare
    }

    // Aggiunge una carta al giocatore
    public void addCard(Player player, Card card) {
        // logica da implementare
        // lo fai
    }
    public void addExtraCard(Player player, Card card) {
        //chiama handleExtraCard
    }

    // Risolve tutti gli eventi visibili inclusa la fila superiore (fine partita Era III)
    public void resolveAllEvents() {
        // logica da implementare
    }

    // Calcola i punti finali di tutti i giocatori
    //possibilmente private, la usiamo dentro getWinner, non deve essere accessibile
    public void countFinalPoints() {
        // logica da implementare
    }

    // Restituisce il giocatore con più PP, in caso di parità considera il cibo
    public Player getWinner() {
        countFinalPoints();
        Player winner = players.get(0);
        for (Player p : players) {
            if (p.getPP() > winner.getPP()) {
                winner = p;
            } else if (p.getPP() == winner.getPP()) {
                if (p.getFood() > winner.getFood())
                    winner = p;
            }
        }
        return winner;
    }

    // Controlla se il giocatore passato è il giocatore attivo
    private boolean isActivePlayer(Player player) {
        return activePlayer == player;

    }

    // Assegna il colore scelto al giocatore e lo rimuove dai disponibili
    private void assignColor(Player player, Color color) {
        player.setColor(color);
        updateAvailableColors(color);
    }

}
