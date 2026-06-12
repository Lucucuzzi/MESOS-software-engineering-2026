package it.polimi.ingsw.am46.server.model;

import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Board.
 */
public class Board {
    private final ArrayList<Card> topRow;
    private final ArrayList<Card> bottomRow;
    private Deck<TribeCard> tribeDeck;
    private Deck<BuildingCard> buildingsEra1;
    private Deck<BuildingCard> buildingsEra2;
    private Deck<BuildingCard> buildingsEra3;
    private final DeckLoader deckLoader;
    private ArrayList<Card> discardCards;
    private final ArrayList<Player> totemOrder;
    private TurnTile turnTile;
    private final ArrayList<OfferTile> offerTiles;

    /**
     * Instantiates a new Board.
     */
    public Board(){
        this.topRow = new ArrayList<>();
        this.bottomRow = new ArrayList<>();
        this.tribeDeck = new Deck<>();
        this.buildingsEra1 = new Deck<>();
        this.buildingsEra2 = new Deck<>();
        this.buildingsEra3 = new Deck<>();
        this.discardCards = new ArrayList<>();
        this.offerTiles = new ArrayList<>();
        this.totemOrder = new ArrayList<>();
        this.deckLoader = new DeckLoader();
    }

    //SET-UP METHODS

    /**
     * Sets bottom row.
     *
     * @param numPlayers the num players
     */
    //This method prepare the bottomRow of the game, it is called only one time at the start of the game
    public void setupBottomRow(int numPlayers) {
        int cardsNeeded = numPlayers + 1;
        while (bottomRow.size() < cardsNeeded) {
            TribeCard drawnCard = tribeDeck.draw();
            if (drawnCard.getType()== Type.EVENT) {
                addCardToTopRow(drawnCard); // if a card is an event card, it goes to the upper row
            } else {
                addCardToBottomRow(drawnCard);
            }
        }
    }

    /**
     * Sets upper row.
     *
     * @param numPlayers the num players
     */
    //This method prepare the UpperRow of the game, it is called only one time at the start of the game
    public void setupUpperRow(int numPlayers) {
        restoreUpperRow(numPlayers); // fill the upper row with tribe cards until it has numPlayers+4 cards

        while (!buildingsEra1.isEmpty()) {
            addCardToTopRow(buildingsEra1.draw());
        }
    }

    /**
     * Sets turn tile.
     *
     * @param numPlayers the num players
     * @param players    the players
     */
    public void setupTurnTile(int numPlayers, List<Player> players) {
        List<Space> spaces = BoardRules.createTurnTileSpaces(numPlayers);
        this.turnTile = new TurnTile(spaces);
        this.turnTile.randomlyPlaceTotems(players);
    }

    /**
     * Sets offer tile.
     *
     * @param numPlayers the num players
     */
    public void setupOfferTile(int numPlayers) {
        List<OfferTile> tiles = BoardRules.createOfferTiles(numPlayers);
        this.offerTiles.addAll(tiles);
    }

    /**
     * Sets tribe deck.
     *
     * @param numPlayers the num players
     */
    // deck is already filtred and shuffled during the loading phase
    public void setupTribeDeck(int numPlayers) {
        this.tribeDeck = deckLoader.loadTribeDeck(numPlayers);
    }

    /**
     * Sets building deck.
     *
     * @param numPlayers the num players
     */
    // the deck is already shuffled during the loading phase
    public void setupBuildingDeck(int numPlayers) {
        this.buildingsEra1 = deckLoader.loadBuildingDeck(numPlayers,1);
        this.buildingsEra2 = deckLoader.loadBuildingDeck(numPlayers,2);
        this.buildingsEra3 = deckLoader.loadBuildingDeck(numPlayers,3);
    }

    //GAMEPLAY METHODS

    /**
     * Discard under without building.
     */
    //This method discard the bottom row cards except for the building card
    public void discardUnderWithoutBuilding() {
        for (Card card : bottomRow) {
            if (card.getType() != Type.BUILDING) {
                discardCards.add(card);
            }
        }
        bottomRow.removeIf(card -> card.getType() != Type.BUILDING);
    }

    /**
     * Discard buildings under.
     */
    //Discard all the building cards in the bottom row, used at the end of the era
    public void discardBuildingsUnder() {
        List<Card> toRemove = new ArrayList<>();
        for (Card card : bottomRow) {
            if (card.getType() == Type.BUILDING) {
                toRemove.add(card);
                discardCards.add(card);
            }
        }
        bottomRow.removeAll(toRemove);
    }

    /**
     * Restore upper row.
     *
     * @param numPlayers the num players
     */
    //Add card to UpperRow until it reach the numPlayers+4 cards, without counting the building cards
    public void restoreUpperRow(int numPlayers) {
        int cardNeeded = numPlayers + 4;

        int currentTribeCards = (int) topRow.stream()
                .filter(c -> c.getType() != Type.BUILDING)
                .count();

        while (currentTribeCards < cardNeeded && !tribeDeck.isEmpty()) {
            addCardToTopRow(tribeDeck.draw());
            currentTribeCards++;
        }
    }


    /**
     * Remove from board.
     *
     * @param card the card
     */
    public void removeFromBoard(Card card) {
        topRow.remove(card);
        bottomRow.remove(card);
    }


    /**
     * Move up to down.
     */
    public void moveUpToDown() {
        List<Card> toMove = new ArrayList<>();
        for (Card c : topRow) {
            if (c.getType() != Type.BUILDING) {
                toMove.add(c);
            }
        }
        topRow.removeAll(toMove);
        bottomRow.addAll(toMove);
    }

    /**
     * Move buildings up to down.
     */
    public void moveBuildingsUpToDown() {
        List<Card> toMove = new ArrayList<>();
        for (Card c : topRow) {
            if (c.getType() == Type.BUILDING) {
                toMove.add(c);
            }
        }
        topRow.removeAll(toMove);
        bottomRow.addAll(toMove);
    }

    /**
     * Restore new era buildings.
     *
     * @param era the era
     */
    public void restoreNewEraBuildings(int era) {
        if (era == 2) {
            while (!buildingsEra2.isEmpty()) {
                addCardToTopRow(buildingsEra2.draw());
            }
        } else if (era == 3) {
            while (!buildingsEra3.isEmpty()) {
                addCardToTopRow(buildingsEra3.draw());
            }
        }
    }

    /**
     * Add card to top row.
     *
     * @param card the card
     */
    public void addCardToTopRow(Card card) {
        if (card != null) {
            this.topRow.add(card);
        }
    }

    /**
     * Add card to bottom row.
     *
     * @param card the card
     */
    public void addCardToBottomRow(Card card) {
        if (card != null) {
            this.bottomRow.add(card);
        }
    }

    /**
     * Add offer tile.
     *
     * @param tile the tile
     */
    public void addOfferTile (OfferTile tile) {
        if (tile != null) {
            this.offerTiles.add(tile);
        }
    }

    /**
     * Gets top row.
     *
     * @return the top row
     */
    public List<Card> getTopRow() {
        return new ArrayList<>(this.topRow);
    }

    /**
     * Gets bottom row.
     *
     * @return the bottom row
     */
    public List<Card> getBottomRow() {
        return new ArrayList<>(this.bottomRow);
    }

    /**
     * Gets offer tiles.
     *
     * @return the offer tiles
     */
    public List<OfferTile> getOfferTiles() {
        return new ArrayList<>(this.offerTiles);
    }

    /**
     * Gets turn tile.
     *
     * @return the turn tile
     */
    public TurnTile getTurnTile() {
        return turnTile;
    }

    /**
     * Gets tribe deck.
     *
     * @return the tribe deck
     */
    public Deck<TribeCard> getTribeDeck() {
        return tribeDeck;
    }
}
