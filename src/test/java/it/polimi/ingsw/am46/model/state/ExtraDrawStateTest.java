package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.server.model.Board;
import it.polimi.ingsw.am46.server.model.Game;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.CardDataDTO;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingFactory;
import it.polimi.ingsw.am46.server.model.cards.characterCards.Builder;
import it.polimi.ingsw.am46.server.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.server.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.server.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.server.model.cards.enums.Item;
import it.polimi.ingsw.am46.server.model.cards.eventCards.Hunt;
import it.polimi.ingsw.am46.server.model.state.ExtraDrawState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraDrawStateTest {
    private Game game;
    private Board board;
    private Player p1, p2;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.addPlayer("P1");
        game.addPlayer("P2");

        p1 = game.getPlayers().get(0);
        p2 = game.getPlayers().get(1);
        board = game.getBoard();

        // Initialize the TurnTile so the FSM doesn't crash when auto-advancing to PlaceTotemState
        board.setupTurnTile(2, game.getPlayers());

        // Base food for tests
        p1.setFood(5);
        p2.setFood(5);

        game.setCurrentPhase(new ExtraDrawState());
    }

    private CardDataDTO.BuildingDTO mockBuildingDTO(int id, int cost, String trigger, String effectId) {
        CardDataDTO.BuildingDTO dto = new CardDataDTO.BuildingDTO();
        dto.id = id; dto.era = 1; dto.cost = cost; dto.pp = 0;
        dto.triggerType = trigger; dto.EffectID = effectId;
        return dto;
    }



    @Test
    void testSecurityValidations() {
        p1.setCanTakeExtraCard(true); // P1 is eligible
        p2.setCanTakeExtraCard(false); // P2 is NOT eligible

        Hunter expensiveTop = new Hunter(1, 1, 10, false, 2);
        Gatherer bottomCard = new Gatherer(2, 1, 0, 2);
        Hunt eventCard = new Hunt(3, 1, 0, false, 0);

        board.addCardToTopRow(expensiveTop);
        board.addCardToTopRow(eventCard);
        board.addCardToBottomRow(bottomCard);

        game.getCurrentPhase().startPhase(game);

        assertEquals(p1, game.getActivePlayer());

        // Unprivileged player tries to draw
        assertThrows(IllegalStateException.class, () -> game.addExtraCard(p2, expensiveTop));

        // Drafting from the bottom row (forbidden for Extra Draws)
        assertThrows(IllegalStateException.class, () -> game.addExtraCard(p1, bottomCard));

        // Drafting an Event
        assertThrows(IllegalStateException.class, () -> game.addExtraCard(p1, eventCard));

        // Drafting without enough food
        p1.setFood(0);
        assertThrows(IllegalStateException.class, () -> game.addExtraCard(p1, expensiveTop));
    }

    @Test
    void testSkipOptionalDraw() {
        p1.setCanTakeExtraCard(true);
        game.getCurrentPhase().startPhase(game);

        // Extra draws are optional. Passing null signifies the player skips their extra draw.
        assertDoesNotThrow(() -> game.addExtraCard(p1, null));

    }

    @Test
    void testBuilderDiscountAndFoodDeduction() {
        p1.setCanTakeExtraCard(true);

        // P1 has 0 food, but has a Builder (-3 cost)
        p1.addCard(new Builder(5001, 1, 0, 0, 3, 2));
        p1.setFood(0);

        BuildingCard bCard = BuildingFactory.createBuilding(mockBuildingDTO(8001, 2, "ADDCARD", "EFFECT1"));
        board.addCardToTopRow(bCard);

        game.getCurrentPhase().startPhase(game);

        //  2 (cost) - 3 (discount) = 0
        assertDoesNotThrow(() -> game.addExtraCard(p1, bCard));
        assertTrue(p1.getBuildings().contains(bCard));
    }

    @Test
    void testPairTrackingAndQueueAdvance() {
        // Both players are eligible this round
        p1.setCanTakeExtraCard(true);
        p2.setCanTakeExtraCard(true);

        p1.addCard(new Inventor(4001, 1, 0, Item.ARROW, 2));
        Inventor match = new Inventor(4002, 1, 0, Item.ARROW, 2);
        board.addCardToTopRow(match);

        Gatherer genericTop = new Gatherer(1001, 1, 2, 2);
        board.addCardToTopRow(genericTop);

        game.getCurrentPhase().startPhase(game);

        assertEquals(p1, game.getActivePlayer());
        game.addExtraCard(p1, match);

        // Check side effects
        assertEquals(1, p1.getNewlyFormedInventorPairs());
        assertFalse(board.getTopRow().contains(match));

        // After P1 finishes, the queue should advance to P2 without ending the phase
        assertEquals(p2, game.getActivePlayer());


        p2.setFood(5);
        game.addExtraCard(p2, genericTop);

        // Check food deduction (5 base - 2 cost = 3 )
        assertEquals(3, p2.getFood());

    }
}