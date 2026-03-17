package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;

import java.util.ArrayList;
import java.util.LinkedList;

public class Board {
    private ArrayList<Card> topRow;
    private ArrayList<Card> bottomRow;
    private Deck<TribeCard> tribeDeck;
    private Deck<BuildingCard> buildingsEra1;
    private Deck<BuildingCard> buildingsEra2;
    private Deck<BuildingCard> buildingsEra3;
    private ArrayList<Card> discardCards;
    private ArrayList<Player> totemOrder;

    public Board(){
        //costruttore valutiamo come usarlo
    }

    public void discardUnder() {
        // implementare
    }


    public LinkedList<Color> getTotemOnTrack() {
        // implement
        return null;
    }

    public void setupOfferTile(int numPlayers) {
        // implement
    }

    public void setupOrderTile(int numPlayers) {
        // implement
    }

    public void setupTribeDeck(int numPlayers) {
        // implement
    }

    public void setupUpperRow(int numPlayers) {
        // implement
    }

    public void restoreUpperRow(int numPlayers) {
        // implement
    }

    public void setupBottomRow(int numPlayers) {
        // implement
    }

    public void setupBuildingDeck(int numPlayers) {
        // implement
    }

    public void removeFromBoard(Card card, ArrayList<Card> upperRow, ArrayList<Card> bottomRow) {
        // implement
    }

    public void repositionTotem() {
        // "metodo per riportare totem su turnTile"
    }

    public void moveUpToDown() {
        // "metodo sposto carte sopra a sotto tranne building"

    }

    public void discardBuildingsLowerEra3() {
        // implement
    }

    public void moveBuildingsUpToDown() {
        // implement
    }

    public void restoreNewEraBuildings() {
        // implement
    }

}
