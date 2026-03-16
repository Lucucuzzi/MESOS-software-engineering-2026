package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import java.util.Deque;

public class Deck<T extends Card> {
    private Deque<T> Cards;
}
