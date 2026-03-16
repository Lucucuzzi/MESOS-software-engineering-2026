package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;

import java.util.ArrayList;

public class Board {
    private ArrayList<Card> topRow;
    private ArrayList<Card> bottomRow;
    private Deck<TribeCard> tribeDeck;
    private Deck<BuildingCard> buildingsEra1;
    private Deck<BuildingCard> buildingsEra2;
    private Deck<BuildingCard> buildingsEra3;
    private ArrayList<Card> discardCards;
    private ArrayList<Player> totemOrder;
}
