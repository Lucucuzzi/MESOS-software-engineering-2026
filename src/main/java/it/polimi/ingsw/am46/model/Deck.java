package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;

import java.util.*;

public class Deck<T extends Card> {
    private final Deque<T> cards;


    public Deck() {
        this.cards = new ArrayDeque<>();
    }

    // pollFirst() returns null if the deck is empty
    public T draw() {
        return cards.pollFirst();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size(){
        return cards.size();
    }

    public void shuffle() {
        List<T> list = new ArrayList<>(this.cards);
        Collections.shuffle(list);
        this.cards.clear();
        this.cards.addAll(list);
    }

    // Adds a list of cards to the bottom of the deck
    public void addAll(List<T> newCards) {
        if (newCards != null && !newCards.isEmpty()) {
            this.cards.addAll(newCards);
        }
    }

    public void addCardToTop(T card) {
        cards.addFirst(card);
    }

    public void addCardToBottom(T card) {
        cards.addLast(card);
    }
}
