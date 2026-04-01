package it.polimi.ingsw.am46.model.state;



import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.state.EndRoundState;
import it.polimi.ingsw.am46.model.state.PlaceTotemState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EndRoundStateTest {

    private Game game;
    private Board board;
    private Player p1, p2;

    @BeforeEach
    void setupBaseGame() throws Exception {
        game = new Game();
        board = game.getBoard();

        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);

        // Setup TurnTile for 2 players
        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 0, 0);
        Space s2 = new Space(2, 0, 0);
        s1.setPlayer(p1);
        s2.setPlayer(p2);
        spaces.add(s1);
        spaces.add(s2);

        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        // Set the starting phase to EndRoundState to test its specific logic
        game.setCurrentPhase(new EndRoundState());
    }

    @Test
    void testStandardRoundCleanup() {
        System.out.println("=== TEST: STANDARD ROUND CLEANUP (NO ERA CHANGE) ===");

        // Setup Top Row: 1 Building, 1 Hunter
        BuildingCard topBuilding = new BuildingCard(101, 1, 0, 0, 0, TriggerType.ADDCARD, null);
        Hunter topHunter = new Hunter(201, 1, 0, false, 2);
        board.getTopRow().addAll(List.of(topBuilding, topHunter));

        // Setup Bottom Row: 1 Building (MUST stay), 1 Gatherer (MUST be discarded)
        BuildingCard bottomBuilding = new BuildingCard(102, 1, 0, 0, 0, TriggerType.ADDCARD, null);
        Gatherer bottomGatherer = new Gatherer(301, 1, 0, 2);
        board.getBottomRow().addAll(List.of(bottomBuilding, bottomGatherer));

        // Fill Tribe Deck with Era 1 cards to avoid triggering a new Era
        for (int i = 0; i < 10; i++) {
            board.getTribeDeck().addCardToTop(new Gatherer(900 + i, 1, 0, 2));
        }

        // Execute the EndRound phase
        game.getCurrentPhase().startPhase(game);

        // Rule: Discard Character/Event cards from the bottom row. Buildings stay.
        assertTrue(board.getBottomRow().contains(bottomBuilding), "Bottom building MUST remain during a standard round end!");
        assertFalse(board.getBottomRow().contains(bottomGatherer), "Bottom Tribe cards MUST be discarded!");

        // Rule: Move top Character/Event cards to the bottom row. Buildings stay.
        assertTrue(board.getBottomRow().contains(topHunter), "Top Tribe cards MUST slide to the bottom row!");
        assertTrue(board.getTopRow().contains(topBuilding), "Top buildings MUST stay in the top row during a standard round end!");
        assertFalse(board.getTopRow().contains(topHunter), "Top Tribe cards should no longer be in the top row!");

        // Rule: Restore Top Row to (Players + 4) Tribe cards
        long currentTribeCards = board.getTopRow().stream().filter(c -> c.getType() != Type.BUILDING).count();
        assertEquals(game.getPlayers().size() + 4, currentTribeCards, "Top row must be replenished with (players + 4) Tribe cards!");

        // Rule: Advance round
        assertEquals(2, game.getRound(), "Round counter should have increased to 2!");
        assertTrue(game.getCurrentPhase() instanceof PlaceTotemState, "FSM should transition to PlaceTotemState for the next round!");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEra2Transition() throws Exception {
        System.out.println("=== TEST: TRANSITION TO ERA II ===");

        // Force an Era 2 trigger by placing an Era 2 card on top of the Tribe deck
        Hunter era2Trigger = new Hunter(999, 2, 0, false, 2);
        board.getTribeDeck().addCardToTop(era2Trigger);

        // Setup Buildings in both rows
        BuildingCard topBuildingEra1 = new BuildingCard(101, 1, 0, 0, 0, TriggerType.ADDCARD, null);
        board.getTopRow().add(topBuildingEra1);

        BuildingCard bottomBuildingEra1 = new BuildingCard(102, 1, 0, 0, 0, TriggerType.ADDCARD, null);
        board.getBottomRow().add(bottomBuildingEra1);

        // Setup Era 2 Buildings deck
        Field buildingsEra2Field = Board.class.getDeclaredField("buildingsEra2");
        buildingsEra2Field.setAccessible(true);
        Deck<BuildingCard> deckEra2 = (Deck<BuildingCard>) buildingsEra2Field.get(board);
        BuildingCard newEra2Building = new BuildingCard(505, 2, 0, 0, 0, TriggerType.ADDCARD, null);
        deckEra2.addCardToTop(newEra2Building);

        // Execute the EndRound phase
        game.getCurrentPhase().startPhase(game);

        // Rule: Discard bottom buildings ONLY at the beginning of Era III.
        // We are entering Era II, so bottom buildings MUST NOT be discarded!
        assertTrue(board.getBottomRow().contains(bottomBuildingEra1), "Bottom buildings MUST NOT be discarded when entering Era II!");

        // Rule: Move top buildings to the bottom row
        assertTrue(board.getBottomRow().contains(topBuildingEra1), "Era I Top Buildings MUST slide to the bottom row when entering a new Era!");
        assertFalse(board.getTopRow().contains(topBuildingEra1), "Era I Top Buildings should no longer be in the top row!");

        // Rule: Place new Era buildings in the top row
        assertTrue(board.getTopRow().contains(newEra2Building), "New Era II Buildings MUST be placed in the top row!");

        // Assert Era has changed
        assertEquals(2, game.getCurrentEra(), "The Game should have updated the Current Era to 2!");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEra3Transition() throws Exception {
        System.out.println("=== TEST: TRANSITION TO ERA III (BUILDING DESTRUCTION) ===");

        // Time travel: Set current era to 2
        Field eraField = Game.class.getDeclaredField("currentEra");
        eraField.setAccessible(true);
        eraField.set(game, 2);

        // Force an Era 3 trigger by placing an Era 3 card on top of the Tribe deck
        Hunter era3Trigger = new Hunter(999, 3, 0, false, 2);
        board.getTribeDeck().addCardToTop(era3Trigger);

        // Setup Buildings in both rows
        BuildingCard topBuildingEra2 = new BuildingCard(201, 2, 0, 0, 0, TriggerType.ADDCARD, null);
        board.getTopRow().add(topBuildingEra2);

        BuildingCard bottomBuildingEra1 = new BuildingCard(102, 1, 0, 0, 0, TriggerType.ADDCARD, null);
        board.getBottomRow().add(bottomBuildingEra1);

        // Setup Era 3 Buildings deck
        Field buildingsEra3Field = Board.class.getDeclaredField("buildingsEra3");
        buildingsEra3Field.setAccessible(true);
        Deck<BuildingCard> deckEra3 = (Deck<BuildingCard>) buildingsEra3Field.get(board);
        BuildingCard newEra3Building = new BuildingCard(606, 3, 0, 0, 0, TriggerType.ADDCARD, null);
        deckEra3.addCardToTop(newEra3Building);

        // Execute the EndRound phase
        game.getCurrentPhase().startPhase(game);

        // Rule: Discard bottom buildings ONLY at the beginning of Era III.
        // We are entering Era III, so bottom buildings MUST be discarded!
        assertFalse(board.getBottomRow().contains(bottomBuildingEra1), "Bottom buildings MUST be discarded when entering Era III!");

        // Rule: Move top buildings to the bottom row
        assertTrue(board.getBottomRow().contains(topBuildingEra2), "Era II Top Buildings MUST slide to the bottom row when entering Era III!");

        // Rule: Place new Era buildings in the top row
        assertTrue(board.getTopRow().contains(newEra3Building), "New Era III Buildings MUST be placed in the top row!");

        // Assert Era has changed
        assertEquals(3, game.getCurrentEra(), "The Game should have updated the Current Era to 3!");
    }

    @Test
    void testGameOverCondition() throws Exception {
        System.out.println("=== TEST: GAME OVER CONDITION & FINAL SCORING ===");

        // Time travel: Set Round 10, Era 3
        Field roundField = Game.class.getDeclaredField("round");
        roundField.setAccessible(true);
        roundField.set(game, 10);

        Field eraField = Game.class.getDeclaredField("currentEra");
        eraField.setAccessible(true);
        eraField.set(game, 3);

        // Setup points for a close match
        p1.modifyPP(15);
        p2.modifyPP(20); // P2 is currently winning

        // Give P1 a game-changing ENDTURN building (worth 10 PP)
        BuildingCard endBuilding = new BuildingCard(111, 1, 0, 0, 0, TriggerType.ENDTURN, ctx -> {
            ctx.getActivePlayer().modifyPP(10);
        });
        p1.addCard(endBuilding);

        // Execute the EndRound phase
        game.getCurrentPhase().startPhase(game);

        // Rule: If Round == 10 and Era == 3, the game ends.
        // The state machine should halt and NOT transition to PlaceTotemState.
        assertTrue(game.getCurrentPhase() instanceof EndRoundState, "FSM should remain in EndRoundState when the game is over!");
        assertTrue(game.isGameOver(), "Game should report isGameOver() == true");

        // Rule: Final points must be calculated.
        // P1 should trigger their building: 15 base + 10 building effect = 25 PP.
        assertEquals(25, p1.getPP(), "Final scoring should have triggered the ENDTURN building effect for P1!");

        // Rule: Determine winner
        List<Player> winners = game.getWinner();
        assertEquals(1, winners.size(), "There should be exactly 1 winner (no tie).");
        assertEquals(p1, winners.get(0), "P1 should have won the game (25 PP vs 20 PP)!");
    }
}