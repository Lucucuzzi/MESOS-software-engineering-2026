package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.server.model.Board;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.cards.enums.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardUnitTest {

    private Board board;

    private Card cardCharacter;
    private Card cardBuilding;
    private Card cardEvent;

    @BeforeEach
    void setUp() {
        board = new Board();

        cardCharacter = new Card(1, 1, 0, Type.CHARACTER) {
            @Override public Type getType() { return Type.CHARACTER; }
            @Override public void addToPlayer(Player p) {}
        };

        cardBuilding = new Card(2, 1, 0, Type.BUILDING) {
            @Override public Type getType() { return Type.BUILDING; }
            @Override public void addToPlayer(Player p) {}
        };

        cardEvent = new Card(3, 1, 0, Type.EVENT) {
            @Override public Type getType() { return Type.EVENT; }
        };
    }

    @Test
    void shouldDiscardUnderWithoutBuilding() {
        board.addCardToBottomRow(cardCharacter);
        board.addCardToBottomRow(cardBuilding);
        board.addCardToBottomRow(cardEvent);

        board.discardUnderWithoutBuilding();

        assertEquals(1, board.getBottomRow().size(), "Should keep exactly 1 card");
        assertEquals(Type.BUILDING, board.getBottomRow().getFirst().getType(), "The remaining card must be a BUILDING");
    }

    @Test
    void shouldDiscardBuildingsUnder() {
        board.addCardToBottomRow(cardCharacter);
        board.addCardToBottomRow(cardBuilding);
        board.addCardToBottomRow(cardEvent);

        board.discardBuildingsUnder();

        assertEquals(2, board.getBottomRow().size(), "Should keep 2 non-building cards");
        assertFalse(board.getBottomRow().contains(cardBuilding), "Building should be discarded");
        assertTrue(board.getBottomRow().contains(cardCharacter), "Character should remain");
        assertTrue(board.getBottomRow().contains(cardEvent), "Event should remain");
    }

    @Test
    void shouldRemoveFromBoard() {
        board.addCardToTopRow(cardCharacter);
        board.addCardToBottomRow(cardBuilding);

        board.removeFromBoard(cardCharacter);
        assertTrue(board.getTopRow().isEmpty(), "Top row should be empty after removal");
        assertTrue(board.getBottomRow().contains(cardBuilding), "Bottom row should be untouched");

        board.removeFromBoard(cardBuilding);
        assertTrue(board.getBottomRow().isEmpty(), "Bottom row should be empty after removal");
    }

    @Test
    void shouldMoveUpToDown() {
        board.addCardToTopRow(cardCharacter);
        board.addCardToTopRow(cardBuilding);

        board.moveUpToDown();

        assertEquals(1, board.getTopRow().size(), "One card should remain in Top Row");
        assertEquals(Type.BUILDING, board.getTopRow().getFirst().getType(), "Buildings must stay in Top Row");

        assertEquals(1, board.getBottomRow().size(), "One card should move to Bottom Row");
        assertEquals(Type.CHARACTER, board.getBottomRow().getFirst().getType(), "Non-buildings must move down");
    }

    @Test
    void shouldMoveBuildingsUpToDown() {
        board.addCardToTopRow(cardCharacter);
        board.addCardToTopRow(cardBuilding);

        board.moveBuildingsUpToDown();

        assertEquals(1, board.getTopRow().size(), "One card should remain in Top Row");
        assertEquals(Type.CHARACTER, board.getTopRow().getFirst().getType(), "Characters must stay in Top Row");

        assertEquals(1, board.getBottomRow().size(), "One card should move to Bottom Row");
        assertEquals(Type.BUILDING, board.getBottomRow().getFirst().getType(), "Buildings must move down");
    }
}