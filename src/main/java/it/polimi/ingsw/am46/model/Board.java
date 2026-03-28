package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Board {
    private final ArrayList<Card> topRow;
    private final ArrayList<Card> bottomRow;
    private final Deck<TribeCard> tribeDeck;
    private final Deck<BuildingCard> buildingsEra1;
    private final Deck<BuildingCard> buildingsEra2;
    private final Deck<BuildingCard> buildingsEra3;
    private final ArrayList<Card> discardCards;
    private final ArrayList<Player> totemOrder;
    private TurnTile turnTile;
    private final ArrayList<OfferTile> offerTiles;

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
    }

    //SET-UP METHOD

    //This method prepare the bottomRow of the game, it is called only one time at the start of the game
    public void setupBottomRow(int numPlayers) {
        int cardsNeeded = numPlayers + 1;
        while (bottomRow.size() < cardsNeeded) {
            TribeCard drawnCard = tribeDeck.draw();
            if (drawnCard.getType()== Type.EVENT) {
                topRow.add(drawnCard); // if a card is an event card, it goes to the upper row
            } else {
                bottomRow.add(drawnCard);
            }
        }
    }

    //This method prepare the UpperRow of the game, it is called only one time at the start of the game
    public void setupUpperRow(int numPlayers) {
        restoreUpperRow(numPlayers); // fill the upper row with tribe cards until it has numPlayers+4 cards

        while (!buildingsEra1.isEmpty()) {
            topRow.add(buildingsEra1.draw());
        }
    }

    public void setupOrderTile(int numPlayers) {
        // implement
    }

    public void setupTribeDeck(int numPlayers) {
        // implement
    }

    public void setupOfferTile(int numPlayers) {
        // implement
    }

    public void setupBuildingDeck(int numPlayers) {
        // implement
    }

    //GAMEPLAY METHODS

    //This method discard the bottom row cards except for the building card
    public void discardUnderWithoutBuilding() {
        // have to keep the building cards
        for (Card card : bottomRow) {
            if (card.getType() != Type.BUILDING) {
                discardCards.add(card);
            }
        }
        bottomRow.removeIf(card -> card.getType() != Type.BUILDING);
    }

    //Discard all the cards in the bottom row, used at the end of the era
    public void discardUnder(){
        discardCards.addAll(bottomRow);
        bottomRow.clear();
    }

    //Add card to UpperRow until it reach the numPlayers+4 cards, without counting the building cards
    public void restoreUpperRow(int numPlayers) {
        int cardNeeded = numPlayers + 4;

        int currentTribeCards = (int) topRow.stream()
                .filter(c -> c.getType() != Type.BUILDING)
                .count();

        while (currentTribeCards < cardNeeded && !tribeDeck.isEmpty()) {
            topRow.add(tribeDeck.draw());
            currentTribeCards++;
        }
    }

    //return the Totem on the Track
    public LinkedList<Player> getTotemOnTrack() {
        LinkedList<Player> order = new LinkedList<>();
        for (OfferTile tile : offerTiles) {
            if (tile.isOccupied()) {
                order.add(tile.getTotem());
            }
        }
        return order;
    }

    public void removeFromBoard(Card card) {
        topRow.remove(card);
        bottomRow.remove(card);
    }

    public void repositionTotem() {
        for (OfferTile tile : offerTiles) {
            if (tile.isOccupied()) {
                turnTile.pushTotem(tile.getTotem()); // Move the totem from the OfferTile to the TurnTile
                tile.removeTotem(); // Free the OfferTile
            }
        }
    }

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

    public void restoreNewEraBuildings(int era) {
        // implement
    }

    public ArrayList<Card> getTopRow() {
        return topRow;
    }
    public ArrayList<Card> getBottomRow() {
        return bottomRow;
    }

    public ArrayList<OfferTile> getOfferTiles() {
        return offerTiles;
    }

    public TurnTile getTurnTile() {
        return turnTile;
    }
}
