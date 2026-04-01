package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraDrawStateTest {

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



        Gatherer topGatherer = new Gatherer(101, 1, 2, 2); // Cost: 2
        Hunt topEvent = new Hunt(102, 1, 0, false, 2); // Event
        board.getTopRow().add(topGatherer);
        board.getTopRow().add(topEvent);

        // Bottom Row: 1 normal Gatherer
        Gatherer bottomGatherer = new Gatherer(201, 1, 2, 2);
        board.getBottomRow().add(bottomGatherer);

        for(int i = 0; i < 5; i++) {
            board.getTribeDeck().addCardToTop(new Gatherer(900+i, 1, 0, 2));
        }

        game.setCurrentPhase(new ExtraDrawState());
    }

    @Test
    void testAutomaticSkipIfNoOneCanDraw() {
        System.out.println("TEST: AUTOMATIC SKIP OF EXTRA DRAW PHASE ");

        // No player has the Extra Draw ability enabled.
        p1.setCanTakeExtraCard(false);
        p2.setCanTakeExtraCard(false);


        game.setCurrentPhase(new ExtraDrawState());


        // ExtraDraw -> ResolveEvent -> EndRound -> PlaceTotem.
        game.getCurrentPhase().startPhase(game);

        assertEquals(2, game.getRound(), "The FSM should have skipped the extra draw, resolved events, and increased the round");
    }

    @Test
    void testDrawPermissionsAndRestrictions() {
        System.out.println(" TEST: EXTRA DRAW RULES AND RESTRICTIONS ");

        // Give permission ONLY to P1 and give him infinite food.
        p1.setCanTakeExtraCard(true);
        p1.modifyFood(20);

        p2.setCanTakeExtraCard(false);
        game.getCurrentPhase().startPhase(game);

        // 1. Verify that the Active Player correctly became P1
        assertEquals(p1, game.getActivePlayer(), "P1 should have been elected Active Player for the Extra Draw!");

        // 2. SECURITY TEST: P2 tries to draw without permission.
        Exception e1 = assertThrows(IllegalStateException.class, () -> {
            game.addExtraCard(p2, board.getTopRow().getFirst());
        });
        System.out.println("Correct: P2 blocked " );

        // 3. ROW RESTRICTION TEST: P1 tries to draw from the Bottom Row
        Exception e2 = assertThrows(IllegalStateException.class, () -> {
            game.addExtraCard(p1, board.getBottomRow().getFirst());
        });
        assertEquals("You can only draw extra cards from the TOP row!", e2.getMessage());
        System.out.println("Correct: Bottom Row draw blocked!");

        // 4. EVENT RESTRICTION TEST: P1 tries to draw an Event from the Top Row
        Exception e3 = assertThrows(IllegalStateException.class, () -> {
            game.addExtraCard(p1, board.getTopRow().get(1)); // The "Hunt" event
        });
        assertEquals("You cannot add an Event Card!", e3.getMessage());
        System.out.println("Correct: Event draw blocked!");
    }

    @Test
    void testFoodCheck() {
        System.out.println("=== TEST: FOOD COST CHECK IN EXTRA DRAW ===");

        p1.setCanTakeExtraCard(true);
        p1.modifyFood(0); // Orazio has 0 food. The card costs 2.

        game.getCurrentPhase().startPhase(game);

        // P1 tries to draw the TopGatherer which costs 2 food.
        Exception e = assertThrows(IllegalStateException.class, () -> {
            game.addExtraCard(p1, board.getTopRow().getFirst());
        });
        assertEquals("Not enough food!", e.getMessage());
        System.out.println("Correct: Draw blocked due to insufficient food!");
    }

    @Test
    void testVoluntarySkip() {
        System.out.println("=== TEST: VOLUNTARY SKIP (NULL CARD) ===");

        // P1 has the ability, but decides not to use it to save food.
        p1.setCanTakeExtraCard(true);
        p1.modifyFood(50);

        game.getCurrentPhase().startPhase(game);

        // P1 passes "null" (indicating the intention to skip).
        // Since P1 was the only authorized player, by skipping, the queue empties
        // and the state machine MUST transition all the way through the next round
        assertDoesNotThrow(() -> {
            game.addExtraCard(p1, null);
        }, "Passing 'null' MUST be allowed to let players skip the optional action!");

        assertEquals(2, game.getRound(), "The FSM should have successfully ended the round after skipping!");

        System.out.println("Correct: Skip handled smoothly and FSM advanced.");
    }

    @Test
    void testSuccessfulExtraDrawAndTransition() {
        System.out.println("TEST: SUCCESSFUL EXTRA DRAW ");


        p1.setCanTakeExtraCard(true);
        p1.modifyFood(5);
        p2.setCanTakeExtraCard(true);
        p2.modifyFood(5);

        // Save card references
        Gatherer topCard = (Gatherer) board.getTopRow().get(0);

        game.getCurrentPhase().startPhase(game);

        // The FSM starts with P1. P1 draws the topCard.
        assertEquals(p1, game.getActivePlayer());
        game.addExtraCard(p1, topCard);

        // Verifications for P1:
        assertTrue(p1.getCharacters().contains(topCard), "P1 must have received the Extra card!");
        assertEquals(3, p1.getFood(), "P1 must have paid 2 Food for the card! (5 - 2 = 3)");
        assertFalse(board.getTopRow().contains(topCard), "The card must have been removed from the Board!");

        // Now the ActivePlayer MUST have become P2!
        assertEquals(p2, game.getActivePlayer(), "The FSM should have passed the turn to the second player in queue (P2)!");

        // P2 decides to skip.
        game.addExtraCard(p2, null);

        // Now that both P1 and P2 have finished the queue, the FSM must have transitioned into the next round.
        assertEquals(2, game.getRound(), "The FSM should have successfully advanced to Round 2!");

        System.out.println("Correct: Draw successful, turns advanced, and transition completed.");
    }
}