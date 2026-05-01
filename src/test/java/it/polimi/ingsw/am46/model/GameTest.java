package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import it.polimi.ingsw.am46.model.state.EndRoundState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testPlayerAndColorManagement() {
        // Controller adding players
        for (int i = 1; i <= 5; i++) {
            game.addPlayer("P" + i);
        }

        // Max capacity is 5
        assertThrows(IllegalStateException.class, () -> game.addPlayer("P6"));

        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);

        // Controller assigning colors
        game.assignColor(p1, Color.RED);
        assertEquals(Color.RED, p1.getColor());
        assertFalse(game.getAvailableColors().contains(Color.RED));

        // Color already taken
        assertThrows(IllegalStateException.class, () -> game.assignColor(p2, Color.RED));
    }

    @Test
    void testSetupAndInitialResources() {
        game.addPlayer("P1");
        game.addPlayer("P2");
        game.addPlayer("P3");

        // Invalid capacity setups
        assertThrows(IllegalArgumentException.class, () -> game.setupGame(1));
        assertThrows(IllegalArgumentException.class, () -> game.setupGame(6));

        game.setupGame(3);

        // TurnTile dictates starting food (2, 3, 3 for 3 players)
        List<Player> turnOrder = game.getBoard().getTurnTile().getTurnOrder();
        assertEquals(2, turnOrder.get(0).getFood());
        assertEquals(3, turnOrder.get(1).getFood());
        assertEquals(3, turnOrder.get(2).getFood());

        assertEquals(1, game.getRound());
        assertEquals(1, game.getCurrentEra());
        assertNotNull(game.getActivePlayer());
    }

    @Test
    void testNicknameUniqueness() {
        game.addPlayer("Orazio");

        assertThrows(IllegalArgumentException.class, () -> {
            game.addPlayer("Orazio");
        });

        assertEquals(1, game.getPlayers().size());
    }



    @Test
    void testRoundResolutionAndEraChange() {
        game.addPlayer("P1");
        game.addPlayer("P2");
        game.setupGame(2);

        int initialRound = game.getRound();

        // Inject an Era 2 Building. Buildings are not discarded at round end,
        // ensuring it survives the row movement and triggers the Era advancement.
        BuildingCard era2Building = new BuildingCard(300, 2, 0, 0, TriggerType.ENDTURN, ctx -> {});
        game.getBoard().addCardToTopRow(era2Building);

        game.resolveRound();

        assertEquals(initialRound + 1, game.getRound());
        assertEquals(2, game.getCurrentEra());

        // push to Era 3 to test the building discard routine in changeEra()
        game.changeEra();
        assertEquals(3, game.getCurrentEra());
    }

    @Test
    void testEndGameScoring() {
        game.addPlayer("P1");
        Player p1 = game.getPlayers().getFirst();

        game.setActivePlayer(p1);
        game.setCurrentPhase(new EndRoundState());

        // BuildingCard without Factory overhead to test endgame adding PP
        BuildingCard bCard = new BuildingCard(8, 1, 0, 10, TriggerType.ENDTURN, ctx -> {});
        p1.addCard(bCard);

        // Calculate final points
        game.countFinalPoints();

        assertEquals(10, p1.getPP());

        // Ensure points aren't counted twice if called again by mistake
        p1.modifyPP(5); // Now at 15
        game.countFinalPoints();
        assertEquals(15, p1.getPP());
    }

    @Test
    void testWinnerCalculationTies() {
        game.addPlayer("P1");
        game.addPlayer("P2");
        game.addPlayer("P3");

        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Player p3 = game.getPlayers().get(2);

        // P1 and P3 have identical PP and Food. P2 has less Food.
        p1.modifyPP(50); p1.modifyFood(10);
        p2.modifyPP(50); p2.modifyFood(5);
        p3.modifyPP(50); p3.modifyFood(10);

        List<Player> winners = game.getWinner();

        assertEquals(2, winners.size());
        assertTrue(winners.contains(p1));
        assertTrue(winners.contains(p3));
        assertFalse(winners.contains(p2));
    }

    @Test
    void testEndGameCondition() {
        game.addPlayer("P1");
        game.addPlayer("P2");
        game.setupGame(2);

        // Current Event Tracker
        Hunt event = new Hunt(401, 1, 0, false, 0);
        game.setCurrentEvent(event);
        assertEquals(event, game.getCurrentEvent());

        assertFalse(game.isGameOver());

        game.changeEra(); // Era 2
        game.changeEra(); // Era 3
        for(int i = 1; i < 10; i++) game.resolveRound(); // Round 10

        game.setCurrentPhase(new EndRoundState());

        assertTrue(game.isGameOver());
    }
}