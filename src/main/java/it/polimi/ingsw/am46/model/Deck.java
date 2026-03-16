package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import java.util.Deque;

public class Deck<T extends Card> {
    private Deque<T> Cards;


    public Deck() {
        // costruttore
    }

    public T draw() {
        // pescare
        return null;
    }

    public boolean isEmpty() {
        // implement
        return false;
    }

    public void shuffle() {
        // rimescolamento (usando Collections.shuffle)
    }

    public void addCardToTop(T card) {
        // inserire in testa
    }

    public void addCardToBottom(T card) {
        // inserire in coda
    }
}
