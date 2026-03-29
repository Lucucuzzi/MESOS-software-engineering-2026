package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.model.state.ResolveEventState;
import it.polimi.ingsw.am46.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.List;

public class Game implements GameContext {
    private Player activePlayer;
    private EventCard currentEvent;
    private final int round;
    private final int currentEra;
    private final ArrayList<Color> availableColors;
    private final Board board;
    private final ArrayList<Player> players;
    private RoundPhase currentPhase;

    // private int pp e food, da ipotizzare infinita

    public Game(){
        this.activePlayer = null;
        this.round = 1;
        this.currentEra = 1;
        this.availableColors = new ArrayList<>(List.of(Color.values()));
        this.players = new ArrayList<>();  // lista vuota, si riempie durante setup
        this.board = new Board();          // inizializza il tabellone
    }

    @Override
    public EventCard getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void setActivePlayer(Player player) {
        this.activePlayer = player;
    }

    @Override
    public void setCurrentPhase(RoundPhase phase) {
        this.currentPhase = phase;
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
    public List<Color> getAvailableColors() {
        return availableColors;
    }
    public Board getBoard() {
        return board;
    }
    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setupGame(int numOfPlayers){
        //da implementare
        //chiamerà in fila tutte le setUp di Board
    }

    // Risolve gli eventi presenti nella fila inferiore
    public void resolveEvents() {
        List<EventCard> orderedEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>();
        for (Card card : board.getBottomRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }

        orderedEvents.addAll(sustenanceEvents);
        if (orderedEvents.isEmpty()) {
            currentEvent = null;
            return;
        }
        Player previousActivePlayer = activePlayer;
        RoundPhase previousPhase = currentPhase;
        currentPhase = new ResolveEventState();
        for (EventCard event : orderedEvents) {
            currentEvent = event;
            for (Player player : players) {
                activePlayer = player;
                currentPhase.triggerBuildingEffects(this, player, currentPhase.getTriggerType());
            }
            event.resolve(this);
            board.removeFromBoard(event);
        }
        //ripristina i valori di activeplayer e phase correnti
        currentEvent = null;
        activePlayer = previousActivePlayer;
        currentPhase = previousPhase;
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
        if (!isActivePlayer(player)) {
            throw new IllegalStateException("It's not your turn!");
        }
        moveTotem(offerTile);
    }

    // Implementa la logica effettiva del movimento del totem
    private void moveTotem(OfferTile offerTile) {
        currentPhase.handlePlaceTotem(this, activePlayer, offerTile);
    }

    // Risolve le fasi di fine round in base alla offerTile
    //chiamera anche RemoveFromBoard
    public void resolveRound() {
        // logica da implementare
    }

    // Aggiunge una carta al giocatore
    public void addCard(Player player, Card card) {
        currentPhase.handleAddCard(this, player, card);
    }
    public void addExtraCard(Player player, Card card) {
        currentPhase.handleDrawExtraCard(this, player, card);
    }

    // Risolve tutti gli eventi visibili inclusa la fila superiore (fine partita Era III)
    public void resolveAllEvents() {
        if (currentEra != 3 || round != 10) {
            throw new IllegalStateException("All events can be resolved only at the end of Era III.");
        }
        List<EventCard> orderedEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>();
        for (Card card : board.getBottomRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }

        for (Card card : board.getTopRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }

        orderedEvents.addAll(sustenanceEvents);
        if (orderedEvents.isEmpty()) {
            currentEvent = null;
            return;
        }
        Player previousActivePlayer = activePlayer;
        RoundPhase previousPhase = currentPhase;
        currentPhase = new ResolveEventState();

        for (EventCard event : orderedEvents) {
            currentEvent = event;
            for (Player player : players) {
                activePlayer = player;
                currentPhase.triggerBuildingEffects(this, player, currentPhase.getTriggerType());
            }
            event.resolve(this);
            board.removeFromBoard(event);
        }
        currentEvent = null;
        activePlayer = previousActivePlayer;
        currentPhase = previousPhase;
    }

    // Calcola i punti finali di tutti i giocatori
    //possibilmente private, la usiamo dentro getWinner, non deve essere accessibile
    public void countFinalPoints() {
        //triggers the end
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
