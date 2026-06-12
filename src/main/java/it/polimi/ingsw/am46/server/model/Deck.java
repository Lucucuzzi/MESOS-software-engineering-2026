package it.polimi.ingsw.am46.server.model;

import it.polimi.ingsw.am46.server.model.cards.Card;

import java.util.*;

/**
 * The type Deck.
 *
 * @param <T> the type parameter
 */
public class Deck<T extends Card> {
    private final Deque<T> cards;


    /**
     * Instantiates a new Deck.
     */
    public Deck() {
        this.cards = new ArrayDeque<>();
    }

    /**
     * Draw t.
     *
     * @return the t
     */
    // pollFirst() returns null if the deck is empty
    public T draw() {
        return cards.pollFirst();
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size(){
        return cards.size();
    }

    /**
     * Shuffle.
     */
    public void shuffle() {
        List<T> list = new ArrayList<>(this.cards);
        Collections.shuffle(list);
        this.cards.clear();
        this.cards.addAll(list);
    }

    /**
     * Add all.
     *
     * @param newCards the new cards
     */
    // Adds a list of cards to the bottom of the deck
    public void addAll(List<T> newCards) {
        if (newCards != null && !newCards.isEmpty()) {
            this.cards.addAll(newCards);
        }
    }

    /**
     * Add card to top.
     *
     * @param card the card
     */
    public void addCardToTop(T card) {
        cards.addFirst(card);
    }

    /**
     * Add card to bottom.
     *
     * @param card the card
     */
    public void addCardToBottom(T card) {
        cards.addLast(card);
    }
}
