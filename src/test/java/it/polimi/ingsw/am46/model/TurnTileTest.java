package it.polimi.ingsw.am46.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnTileTest {

    private Space space1;
    private Space space2;
    private Space space3;
    private TurnTile turnTile;

    private Player p1;
    private Player p2;
    private Player p3;

    @BeforeEach
    void setUp() {
        space1 = new Space(1, 1, 0);
        space2 = new Space(2, -2, 0);
        space3 = new Space(3, -2, -2);

        turnTile = new TurnTile(List.of(space1, space2, space3));

        p1 = new Player("Player 1");
        p2 = new Player("Player 2");
        p3 = new Player("Player 3");
    }


    @Test
    void shouldPushTotemToFirstAvailableSpace() {
        space1.setPlayer(p1);
        turnTile.pushTotem(p2);

        assertEquals(p1, space1.getPlayer(), "Space 1 should still have Player 1");
        assertEquals(p2, space2.getPlayer(), "Space 2 should have Player 2");
        assertFalse(space3.isOccupied(), "Space 3 should be empty");
    }

    @Test
    void shouldThrowExceptionWhenPlayerAlreadyOnTile() {
        space1.setPlayer(p1);

        assertThrows(IllegalArgumentException.class, () -> turnTile.pushTotem(p1));
        assertFalse(space2.isOccupied(), "Space 2 should remain empty");
    }

    @Test
    void shouldThrowExceptionWhenTileIsFull() {
        space1.setPlayer(p1);
        space2.setPlayer(p2);
        space3.setPlayer(p3);

        Player p4 = new Player("Player 4");

        assertThrows(IllegalStateException.class, () -> turnTile.pushTotem(p4));
    }

    @Test
    void shouldTakeTotemFromCorrectPosition() {
        space2.setPlayer(p1);

        assertEquals(p1, turnTile.takeTotem(2), "Should return the player at position 2");
        assertFalse(space2.isOccupied(), "Space 2 should now be empty");
        assertNull(turnTile.takeTotem(2), "Space 2 player should be null");
    }

    @Test
    void shouldApplyTTEffectBonus() {
        space1.setPlayer(p1);

        int initialFood = p1.getFood();
        int initialPP = p1.getPP();

        turnTile.applyTTEffect(space1);

        assertEquals(initialFood + 1, p1.getFood(), "Player should gain 1 food");
        assertEquals(initialPP, p1.getPP(), "PP should remain unchanged");
    }

    @Test
    void shouldApplyTTEffectPenaltyWithEnoughFood() {
        space2.setPlayer(p1);

        p1.modifyFood(3);
        int foodBefore = p1.getFood();
        int ppBefore = p1.getPP();

        turnTile.applyTTEffect(space2);

        assertEquals(foodBefore - 2, p1.getFood(), "Player should lose 2 food");
        assertEquals(ppBefore , p1.getPP(), "PP should remain unchanged");
    }

    @Test
    void shouldApplyTTEffectPenaltyWithoutEnoughFood() {
        space3.setPlayer(p1);

        p1.modifyFood(1);
        int foodBefore = p1.getFood();
        int ppBefore = p1.getPP();

        turnTile.applyTTEffect(space3);

        assertEquals(foodBefore, p1.getFood(), "Food should remain unchanged if insufficient");
        assertEquals(ppBefore - 2, p1.getPP(), "Player should have a net loss of 2 PP");
    }

    @Test
    void shouldDoNothingWhenApplyingEffectOnEmptySpace() {
        assertDoesNotThrow(() -> turnTile.applyTTEffect(space1));
    }

    @Test
    void shouldReturnCorrectTurnOrder() {
        space1.setPlayer(p1);
        space3.setPlayer(p2);

        List<Player> order = turnTile.getTurnOrder();

        assertEquals(2, order.size());
        assertEquals(p1, order.get(0));
        assertEquals(p2, order.get(1));
    }

    @Test
    void shouldRandomlyPlaceTotems() {
        List<Player> players = List.of(p1, p2);

        turnTile.randomlyPlaceTotems(players);

        long occupiedCount = List.of(space1, space2, space3).stream()
                .filter(Space::isOccupied)
                .count();

        assertEquals(2, occupiedCount, "Exactly 2 spaces should be occupied");

        List<Player> order = turnTile.getTurnOrder();
        assertTrue(order.contains(p1));
        assertTrue(order.contains(p2));
    }

    @Test
    void shouldGetSpaceOfPlayer() {
        space2.setPlayer(p1);

        Space result = turnTile.getSpaceOfPlayer(p1);
        assertEquals(space2, result);

    }
}