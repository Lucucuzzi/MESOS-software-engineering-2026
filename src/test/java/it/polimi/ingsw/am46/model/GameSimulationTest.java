package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.TribeCard;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.state.AddCardState;
import it.polimi.ingsw.am46.model.state.ExtraDrawState;
import it.polimi.ingsw.am46.model.state.PlaceTotemState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test simulating a full 10-round progression of the "Mesos" board game.
 *
 * Purpose:
 * This is a "Smoke Test" designed to verify the robustness of the core Finite State Machine (FSM).
 * It ensures the game can smoothly transition through all mandatory phases (Place Totem, Draft Card,
 * Extra Draw) across 10 rounds and successfully trigger the End-Game state without stalling or
 * throwing unhandled exceptions.
 *
 * Implementation Details:
 * - Uses Java Reflection to inject a deterministic, hardcoded deck into the internal Board.
 * This guarantees we don't run out of cards during the simulation while strictly preserving
 * the Model's encapsulation (no test-only setters were added to production code).
 * - Intentionally omits strict mathematical assertions on Food or Prestige Points, as those
 * calculations are thoroughly covered by isolated Unit Tests. The focus here is purely on
 * state progression and game flow.
 */

public class GameSimulationTest {

    private Game game;
    private Player alice;
    private Player bob;

    private Player lastDraftPlayer;
    private int remainingTop;
    private int remainingBottom;

    @BeforeEach
    void setUp() throws Exception {
        game = new Game();
        game.addPlayer("Alice");
        game.addPlayer("Bob");

        alice = game.getPlayers().get(0);
        bob = game.getPlayers().get(1);

        game.assignColor(alice, game.getAvailableColors().get(0));
        game.assignColor(bob, game.getAvailableColors().get(1));

        game.setupGame(2);

        Board board = game.getBoard();

        Deck<TribeCard> deterministicTribe = new Deck<>();
        deterministicTribe.addAll(buildDeterministicTribeDeck());

        setPrivateField(board, "tribeDeck", deterministicTribe);
        setPrivateField(board, "buildingsEra1", new Deck<>());
        setPrivateField(board, "buildingsEra2", new Deck<>());
        setPrivateField(board, "buildingsEra3", new Deck<>());

        clearBoardRows(board);
        board.setupBottomRow(2);
        board.setupUpperRow(2);

        lastDraftPlayer = null;
        remainingTop = 0;
        remainingBottom = 0;
    }

    @Test
    void testTenRoundProgression() {
        assertEquals(PlaceTotemState.class, game.getCurrentPhase().getClass());

        for (int round = 1; round <= 10; round++) {
            int aliceBefore = alice.getCards().size();
            int bobBefore = bob.getCards().size();

            placeTotems();
            draftCards();
            resolveExtraDraw();

            assertTrue(alice.getCards().size() > aliceBefore);
            assertTrue(bob.getCards().size() > bobBefore);

            if (round < 10) {
                assertEquals(round + 1, game.getRound());
                assertEquals(PlaceTotemState.class, game.getCurrentPhase().getClass());
            }
        }

        assertTrue(game.getCurrentEra() >= 3);
        assertTrue(game.isGameOver());
        assertFalse(game.getWinner().isEmpty());
    }

    private void placeTotems() {
        while (game.getCurrentPhase().getClass().equals(PlaceTotemState.class)) {
            OfferTile tile = firstFreeTile(game.getBoard().getOfferTiles());
            assertNotNull(tile);
            game.moveTotem(game.getActivePlayer(), tile);
        }
    }

    private void draftCards() {
        while (game.getCurrentPhase().getClass().equals(AddCardState.class)) {
            Player active = game.getActivePlayer();

            if (active != lastDraftPlayer) {
                OfferTile tile = findTileOfPlayer(active);
                remainingTop = tile != null ? tile.getNumCardFromAbove() : 0;
                remainingBottom = tile != null ? tile.getNumCardFromDown() : 0;

                if (game.getBoard().getTopRow().isEmpty()) {
                    remainingTop = 0;
                }
                long bottomAvailable = game.getBoard().getBottomRow().stream()
                        .filter(c -> c.getType() != Type.EVENT)
                        .count();
                if (bottomAvailable == 0) {
                    remainingBottom = 0;
                }

                lastDraftPlayer = active;
            }

            Card pick = null;
            boolean fromTop = false;

            if (remainingTop > 0) {
                pick = firstNonEvent(game.getBoard().getTopRow());
                fromTop = pick != null;
            }

            if (pick == null && remainingBottom > 0) {
                pick = firstNonEvent(game.getBoard().getBottomRow());
                fromTop = false;
            }

            assertNotNull(pick);
            game.addCard(active, pick);

            if (fromTop) remainingTop--;
            else remainingBottom--;
        }
    }

    private void resolveExtraDraw() {
        while (game.getCurrentPhase().getClass().equals(ExtraDrawState.class)) {
            game.addExtraCard(game.getActivePlayer(), null);
        }
    }

    private OfferTile firstFreeTile(List<OfferTile> tiles) {
        for (OfferTile t : tiles) {
            if (!t.isOccupied()) return t;
        }
        return null;
    }

    private OfferTile findTileOfPlayer(Player player) {
        for (OfferTile t : game.getBoard().getOfferTiles()) {
            if (t.getTotem() == player) return t;
        }
        return null;
    }

    private Card firstNonEvent(List<Card> row) {
        for (Card c : row) {
            if (c.getType() != Type.EVENT) return c;
        }
        return null;
    }

    private List<TribeCard> buildDeterministicTribeDeck() {
        List<TribeCard> cards = new ArrayList<>();

        cards.add(new Builder(1001, 2, 0, 3, 1, 2));
        cards.add(new Inventor(2001, 3, 0, Item.SHIELD, 2));
        cards.add(new Artist(3001, 1, 0, 2));
        cards.add(new Hunter(4001, 1, 0, true, 2));
        cards.add(new Builder(1002, 2, 0, 3, 1, 2));
        cards.add(new Inventor(2002, 3, 0, Item.BOAT, 2));

        for (int i = 0; i < 120; i++) {
            int era = (i % 3) + 1;
            switch (i % 4) {
                case 0 -> cards.add(new Builder(5000 + i, era, 0, 3, 1, 2));
                case 1 -> cards.add(new Inventor(6000 + i, era, 0, Item.SHIELD, 2));
                case 2 -> cards.add(new Artist(7000 + i, era, 0, 2));
                case 3 -> cards.add(new Hunter(8000 + i, era, 0, true, 2));
            }
        }
        return cards;
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void clearBoardRows(Board board) throws Exception {
        Field top = board.getClass().getDeclaredField("topRow");
        Field bottom = board.getClass().getDeclaredField("bottomRow");
        top.setAccessible(true);
        bottom.setAccessible(true);
        ((List<?>) top.get(board)).clear();
        ((List<?>) bottom.get(board)).clear();
    }
}