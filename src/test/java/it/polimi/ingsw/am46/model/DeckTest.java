package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.server.model.Deck;
import it.polimi.ingsw.am46.server.model.cards.characterCards.Artist;
import it.polimi.ingsw.am46.server.model.cards.characterCards.CharacterCard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    @Test
    void shouldHandleDeckOperationsCorrectly() {
        Deck<CharacterCard> deck = new Deck<>();

        // Test initial state
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
        assertNull(deck.draw(), "Drawing from an empty deck must return null");

        // Test multiple insertions
        List<CharacterCard> cards = Arrays.asList(
                new Artist(1, 1, 0, 2),
                new Artist(2, 1, 0, 3)
        );
        deck.addAll(cards);
        assertFalse(deck.isEmpty());
        assertEquals(2, deck.size());

        // Test insertion at the top and bottom
        CharacterCard topCard = new Artist(3, 1, 0, 4);
        CharacterCard bottomCard = new Artist(4, 1, 0, 5);
        deck.addCardToTop(topCard);
        deck.addCardToBottom(bottomCard);
        assertEquals(4, deck.size());

        // Test draw (must return the card placed on top)
        CharacterCard drawn = deck.draw();
        assertEquals(topCard.getId(), drawn.getId());
        assertEquals(3, deck.size());

        // Test shuffle
        deck.shuffle();
        assertEquals(3, deck.size(), "Shuffle must not alter the number of cards in the deck");
    }

}