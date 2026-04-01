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

    //SET-UP METHODS

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
        for (Card card : bottomRow) {
            if (card.getType() != Type.BUILDING) {
                discardCards.add(card);
            }
        }
        bottomRow.removeIf(card -> card.getType() != Type.BUILDING);
    }

    //Discard all the building cards in the bottom row, used at the end of the era
    public void discardBuildingsUnder() {
        List<Card> toRemove = new ArrayList<>();
        for (Card card : bottomRow) {
            if (card.getType() == Type.BUILDING) {
                toRemove.add(card);
                discardCards.add(card); // Le aggiungiamo agli scarti
            }
        }
        bottomRow.removeAll(toRemove);
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


    public void removeFromBoard(Card card) {
        topRow.remove(card);
        bottomRow.remove(card);
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
        if (era == 2) {
            while (!buildingsEra2.isEmpty()) {
                topRow.add(buildingsEra2.draw());
            }
        } else if (era == 3) {
            while (!buildingsEra3.isEmpty()) {
                topRow.add(buildingsEra3.draw());
            }
        }
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

    public Deck<TribeCard> getTribeDeck() {
        return tribeDeck;
    }
}
