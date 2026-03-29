package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.model.state.PlaceTotemState;
import it.polimi.ingsw.am46.model.state.ResolveEventState;
import it.polimi.ingsw.am46.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.List;

public class Game implements GameContext {
    private Player activePlayer;
    private EventCard currentEvent;
    private int round;
    private int currentEra;
    private final ArrayList<Color> availableColors;
    private final Board board;
    private final ArrayList<Player> players;
    private RoundPhase currentPhase;
    private boolean finalPointsCounted = false;
    private ArrayList<Player> turnQueue;

    // private int pp e food, da ipotizzare infinita

    public Game(){
        this.activePlayer = null;
        this.round = 1;
        this.currentEra = 1;
        this.availableColors = new ArrayList<>(List.of(Color.values()));
        this.players = new ArrayList<>();  // lista vuota, si riempie durante setup
        this.board = new Board();          // inizializza il tabellone
        this.turnQueue = new ArrayList<>();
    }

    @Override
    public EventCard getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void setActivePlayer(Player player) {
        this.activePlayer = player;
    }


    //Initialize turnQueue for a specific phase and sets the first player
    // Ex: ctx.setTurnQueue(ctx.getBoard().getTurnTile().getTurnOrder());

    public void setTurnQueue(List<Player> players) {
        this.turnQueue = new ArrayList<>(players); // Copiamo la lista per modificarla in sicurezza
        advanceTurn(); // Imposta subito il primo giocatore
    }


    // Viene passato come lista o la turnTile o la lista delle OfferTile, prende il giocatore più a sinistra lo setta come attivo
    public void setActivePlayer(ArrayList<Player> p) {
        ArrayList<Player> players = new ArrayList<>(p);
        if(!players.isEmpty()){
            setActivePlayer(players.getFirst());
            players.removeFirst();
            //qua si potrebbe direttamente spostare da una lista all'altra
        }
        else{
            currentPhase.nextPhase();
        }
    }
    /**
     * Consuma il giocatore corrente, imposta il successivo, o cambia fase se la coda è finita.
     */
    public void advanceTurn() {
        if (!turnQueue.isEmpty()) {
            setActivePlayer(turnQueue.removeFirst()); // Takes the first and REMOVE IT FROM THE QUEUE
        } else {
            // If the queue is empty, the phase has finished its round of players!
            activePlayer = null;
            currentPhase.nextPhase(this);
        }
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
        //Validate player count
        if (numOfPlayers < 2 || numOfPlayers > 6) {
            throw new IllegalArgumentException("Game requires 2-6 players, got: " + numOfPlayers);
        }

        //STEP 1: Setup card decks
        board.setupBuildingDeck(numOfPlayers);
        board.setupTribeDeck(numOfPlayers);

        //STEP 2: Setup board components
        board.setupOfferTile(numOfPlayers);
        board.setupOrderTile(numOfPlayers);

        //STEP 3: Draw initial cards
        board.setupBottomRow(numOfPlayers);
        board.setupUpperRow(numOfPlayers);

        // STEP 5: Distribute starting Food based on the new random turn order
        assignInitialResources();


        //STEP 4: Initialize game state
        currentPhase = new PlaceTotemState();

        //Start the phase
        if (!players.isEmpty()) {
            /*we need to set the Active player here or not?  NEED TO COMPLETE*/

            currentPhase.startPhase(this);
        }
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
        this.currentEra++;
        board.discardUnder();
        board.moveBuildingsUpToDown();
        board.restoreNewEraBuildings(this.currentEra);
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

    // clear the board for the next round and increase round
    public void resolveRound() {
        board.discardUnderWithoutBuilding();
        board.moveUpToDown();
        board.restoreUpperRow(players.size());
        boolean newEraTriggered = false;
        for (Card card : board.getTopRow()) {
            if (card.getEra() > this.currentEra) {
                newEraTriggered = true;
                break;
            }
        }
        if (newEraTriggered) {
            changeEra();
        }
        this.round++;
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
        // Prevent double-counting
        if (finalPointsCounted) {
            return;
        }


        Player previousActivePlayer = activePlayer;

        // Apply end-game building effects for ALL players
        // Each player's buildings with ENDTURN trigger will apply their effects
        //(via BuildingFactory lamda functions)

        for (Player player : players) {
            // Set player as active so building effects apply to the correct player
            activePlayer = player;

            // Trigger all building effects with ENDTURN trigger type
            currentPhase.triggerBuildingEffects(this, player, TriggerType.ENDTURN);
        }

        // Restore the previous active player
        activePlayer = previousActivePlayer;

        finalPointsCounted = true;
    }

    // Restituisce il giocatore con più PP, in caso di parità considera il cibo
    public Player getWinner() {
        // ensure we have players in the game
        if (players.isEmpty()) {
            return null;
        }

        // ensure points have been counted correctly
        if (!finalPointsCounted) {
            throw new IllegalStateException("countFinalPoints() must be called before getWinner()");
        }

        Player winner = players.get(0);

        for (Player player : players) {
            // Primary criteria: Prestige Points
            if (player.getPP() > winner.getPP()) {
                winner = player;
            }
            // Tiebreaker: Food amount
            else if (player.getPP() == winner.getPP() &&
                    player.getFood() > winner.getFood()) {
                winner = player;
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
    public boolean isGameOver() {
        return currentEra == 3 && round == 10 && currentPhase.isFinalPhase();
    }

    private void assignInitialResources() {
        // We get the list of players perfectly ordered by their position on the TurnTile
        List<Player> startingOrder = board.getTurnTile().getTurnOrder();

        int[] startingFood = {2, 3, 3, 4, 4};

        for (int i = 0; i < startingOrder.size(); i++) {
            Player p = startingOrder.get(i);
            // Assign the resources from the array based on their index
            p.modifyFood(startingFood[i]);

        }
    }
}
