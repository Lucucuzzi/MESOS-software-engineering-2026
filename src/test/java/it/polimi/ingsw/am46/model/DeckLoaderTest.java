package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.server.model.Deck;
import it.polimi.ingsw.am46.server.model.DeckLoader;
import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.server.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.server.model.cards.eventCards.EventCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckLoaderTest {

    private DeckLoader deckLoader;

    @BeforeEach
    void setUp() {
        // Uses JSON in resources
        deckLoader = new DeckLoader();
    }

    @Test
    void shouldLoadBuildingDeckRespectingBoardRulesExactSizes() {
        // 2 Players: Era 1 -> 1 card
        assertEquals(1, deckLoader.loadBuildingDeck(2, 1).size(), "Era 1 buildings for 2 players should be 1");

        // 4 Players: Era 2 -> 3 cards
        assertEquals(3, deckLoader.loadBuildingDeck(4, 2).size(), "Era 2 buildings for 4 players should be 3");

        // 5 Players: Era 3 -> 5 cards
        assertEquals(5, deckLoader.loadBuildingDeck(5, 3).size(), "Era 3 buildings for 5 players should be 5");
    }

    @Test
    void shouldLoadBuildingDeckContainingOnlyRequestedEra() {
        Deck<BuildingCard> deck = deckLoader.loadBuildingDeck(3, 2);
        assertFalse(deck.isEmpty(), "Building deck should not be empty");

        while (!deck.isEmpty()) {
            assertEquals(2, deck.draw().getEra(), "Building card must match the requested era");
        }
    }

    @Test
    void shouldFilterTribeDeckByMinPlayers() {
        Deck<TribeCard> deck4P = deckLoader.loadTribeDeck(4);
        assertFalse(deck4P.isEmpty(), "Tribe deck should not be empty");

        while (!deck4P.isEmpty()) {
            if (deck4P.draw() instanceof CharacterCard c) {
                assertTrue(c.getMinPlayers() <= 4, "Card requires too many players");
            }
        }
    }

    @Test
    void shouldLoadEveryTribeCard(){
        assertEquals(96, deckLoader.loadTribeDeck(5).size());
    }

    @Test
    void shouldLoadTribeDeckWithEra1OnTopAndFinalEventOnBottom() {
        Deck<TribeCard> deck = deckLoader.loadTribeDeck(5);
        assertFalse(deck.isEmpty(), "Tribe deck should not be empty");

        // 1. Top card check
        assertEquals(1, deck.draw().getEra(), "The top card must belong to Era 1");

        // 2. Drain the deck to find the bottom card
        TribeCard bottomCard = null;
        while (!deck.isEmpty()) {
            bottomCard = deck.draw();
        }

        // 3. Bottom card check using Pattern Matching (No explicit casting needed)
        assertTrue(bottomCard instanceof EventCard event && event.isFinalEvent(),
                "The absolute bottom card must be a FINAL Event");
    }
}