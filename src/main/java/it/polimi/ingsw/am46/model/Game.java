package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;
import it.polimi.ingsw.am46.model.state.PlaceTotemState;
import it.polimi.ingsw.am46.model.state.RoundPhase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game implements GameContext {
    //COMMENT TO TEST COMMIT
    private Player activePlayer;
    private EventCard currentEvent;
    private int round;
    private int currentEra;
    private final ArrayList<Color> availableColors;
    private final Board board;
    private final ArrayList<Player> players;
    private RoundPhase currentPhase;
    private boolean finalPointsCounted = false;
    private int numOfPlayers;
    private String hostNickname;
    private Integer expectedPlayers;
    private boolean gameStarted;
    private PhaseChangeListener phaseChangeListener;

    // private int pp and food, assumed to be infinite
    public Game(){
        this.activePlayer = null;
        this.round = 1;
        this.currentEra = 1;
        this.availableColors = new ArrayList<>(List.of(Color.values()));
        this.players = new ArrayList<>();  // empty list, filled during setup
        this.board = new Board();          // initializes the board
        this.numOfPlayers = 0;
        this.hostNickname = null;
        this.expectedPlayers = null;
        this.gameStarted = false;
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
        if (gameStarted && phaseChangeListener != null) {
            phaseChangeListener.onPhaseChanged();
        }
    }

    @Override
    public void setCurrentEvent(EventCard currentEvent) {
        this.currentEvent = currentEvent;
    }

    @Override
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
    public String getHostNickname() {
        return hostNickname;
    }
    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }
    public Integer getExpectedPlayers() {
        return expectedPlayers;
    }
    public void setExpectedPlayers(Integer expectedPlayers) {
        this.expectedPlayers = expectedPlayers;
    }
    public boolean isGameStarted() {
        return gameStarted;
    }
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
    public void setPhaseChangeListener(PhaseChangeListener phaseChangeListener) {
        this.phaseChangeListener = phaseChangeListener;
    }


    public void setupGame(int numOfPlayers){
        this.numOfPlayers = numOfPlayers;
        //Validate player count
        if (numOfPlayers < 2 || numOfPlayers > 5) {
            throw new IllegalArgumentException("Game requires 2-5 players, got: " + numOfPlayers);
        }

        //Setup card decks
        board.setupBuildingDeck(numOfPlayers);
        board.setupTribeDeck(numOfPlayers);

        //Setup board components
        board.setupOfferTile(numOfPlayers);
        board.setupTurnTile(numOfPlayers, players);

        //Draw initial cards
        board.setupBottomRow(numOfPlayers);
        board.setupUpperRow(numOfPlayers);

        // Distribute starting Food based on the new random turn order
        assignInitialResources();

        //Initialize game state
        currentPhase = new PlaceTotemState();
        currentPhase.startPhase(this);
        this.gameStarted = true;

    }


    // Removes the chosen color from the available colors and updates the private field
    public void updateAvailableColors(Color color) {
        availableColors.remove(color);
    }

    // Changes the current era and updates the available cards
    public void changeEra() {
        this.currentEra++;
        if (this.currentEra == 3) {
            board.discardBuildingsUnder();
        }
        board.moveBuildingsUpToDown();
        board.restoreNewEraBuildings(this.currentEra);
    }

    // Checks that the player can move the totem onto the specified offer tile
    public void moveTotem(Player player, OfferTile offerTile) {
        if (!isActivePlayer(player)) {
            throw new IllegalStateException("It's not your turn!");
        }
        moveTotem(offerTile);
    }

    // Implements the actual totem movement logic
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

    // Adds a card to the player
    public void addCard(Player player, Card card) {
        currentPhase.handleAddCard(this, player, card);
    }
    public void addExtraCard(Player player, Card card) {
        currentPhase.handleDrawExtraCard(this, player, card);
    }

    // Calculates the final points of all players
    // possibly private, we use it inside getWinner, it should not be accessible
    public void countFinalPoints() {
        // Prevent double-counting
        if (finalPointsCounted) {
            return;
        }
        Player previousActivePlayer = activePlayer;

        // Apply end-game building effects for ALL players
        // Each player's buildings with ENDTURN trigger will apply their effects
        //(via BuildingFactory lambda functions)
        for (Player player : players) {
            // Set player as active so building effects apply to the correct player
            activePlayer = player;
            currentPhase.triggerBuildingEffects(this, player, TriggerType.ENDTURN);
            for (BuildingCard building : player.getBuildings()) {
                player.modifyPP(building.getPp());
            }
            //-- adding final extra PP for builderPP, Artists pairs, Inventors unique item
            player.modifyPP(player.calculateBuilderPP());
            player.modifyPP(player.calculateArtistBonus());
            player.modifyPP(player.calculateInventorBonus());
        }
        // Restore the previous active player
        activePlayer = previousActivePlayer;
        finalPointsCounted = true;
    }

    // Returns the player with the most PP; in case of a tie, considers food
    public List<Player> getWinner() {
        List<Player> winners = new ArrayList<>();
        Player topPlayer = players.getFirst();
        winners.add(topPlayer);

        for (int i = 1; i < players.size(); i++) {
            Player current = players.get(i);

            if (current.getPP() > topPlayer.getPP()) {
                topPlayer = current;
                winners.clear();
                winners.add(current);
            }

            else if (current.getPP() == topPlayer.getPP()) {
                if (current.getFood() > topPlayer.getFood()) {
                    topPlayer = current;
                    winners.clear();
                    winners.add(current);
                }

                else if (current.getFood() == topPlayer.getFood()) {
                    winners.add(current);
                }
            }
        }

        return winners;
    }

    // Checks whether the given player is the active player
    private boolean isActivePlayer(Player player) {
        return activePlayer == player;

    }

    // Assigns the chosen color to the player and removes it from the available ones
    public void assignColor(Player player, Color color) {
        if (!availableColors.contains(color)) {
            throw new IllegalStateException("Color is not available!");
        }
        player.setColor(color);
        updateAvailableColors(color);
    }
    public boolean isGameOver() {
        return currentEra == 3 && round == 10 && currentPhase.isFinalPhase();
    }

    private void assignInitialResources() {
        List<Player> startingOrder = board.getTurnTile().getTurnOrder();

        int[] startingFood = {2, 3, 3, 4, 4};

        for (int i = 0; i < startingOrder.size(); i++) {
            Player p = startingOrder.get(i);
            p.modifyFood(startingFood[i]);

        }
    }

    public RoundPhase getCurrentPhase() {
        return currentPhase;
    }

    //addPlayer to add the players at the game
    public void addPlayer(String nickname){
        if (players.size()>=5) {
            throw new IllegalStateException("Maximum capacity for the match reached"); }
        if(players.contains(getPlayerByNickname(nickname))){
            throw new IllegalArgumentException("Player already exists!");
        }

        else{
            Player player = new Player(nickname);
            players.add(player);
        }
    }

    public void removePlayer(String nickname) {
        players.removeIf(player -> player.getNickname().equals(nickname));
    }

    private Player getPlayerByNickname(String nickname) {
        for (Player player : players) {
            if (player.getNickname().equals(nickname)) {
                return player;
            }
        }
        return null;
    }

}
