package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.CardDataDTO;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingFactory;
import it.polimi.ingsw.am46.model.cards.characterCards.Builder;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddCardStateTest {
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

        // Init TurnTile for 2 players
        board.setupTurnTile(2, game.getPlayers());

        // Since we skip PlaceTotemState in this unit test, we have to manually
        // free up the TurnTile slots. Otherwise, the game crashes when the FSM
        // tries to place the player's totem back at the end of the draft!
        for (int i = 1; i <= 5; i++) {
            board.getTurnTile().takeTotem(i);
        }

        p1.setFood(5);
        p2.setFood(5);

        game.setCurrentPhase(new AddCardState());
    }

    // Helper to bypass JSON loading when creating test buildings
    private CardDataDTO.BuildingDTO mockBuildingDTO(int id, int cost, String trigger, String effectId) {
        CardDataDTO.BuildingDTO dto = new CardDataDTO.BuildingDTO();
        dto.id = id; dto.era = 1; dto.cost = cost; dto.pp = 0;
        dto.triggerType = trigger; dto.EffectID = effectId;
        return dto;
    }

    @Test
    void testSecurityValidations() {
        OfferTile tile = new OfferTile('A', 1, 1, 0, 0);
        tile.placeTotem(p1);
        board.addOfferTile(tile);

        Hunter expensiveCard = new Hunter(1001, 1, 10, false, 2);
        Gatherer missingCard = new Gatherer(9000, 1, 0, 2);
        Hunt eventCard = new Hunt(3001, 1, 0, false, 0);

        board.addCardToBottomRow(new Gatherer(2001, 1, 0, 2));

        board.addCardToTopRow(expensiveCard);
        board.addCardToTopRow(eventCard);

        game.getCurrentPhase().startPhase(game);

        assertEquals(p1, game.getActivePlayer());

        // Wrong player
        assertThrows(IllegalStateException.class, () -> game.addCard(p2, expensiveCard));

        // Drafting an Event
        assertThrows(IllegalStateException.class, () -> game.addCard(p1, eventCard));

        // Card not on board
        assertThrows(IllegalArgumentException.class, () -> game.addCard(p1, missingCard));

        // Not enough food
        p1.setFood(0);
        assertThrows(IllegalStateException.class, () -> game.addCard(p1, expensiveCard));
    }

    @Test
    void testDraftLimits() {
        // Tile allows 2 from top, 1 from bottom
        OfferTile tile = new OfferTile('A', 1, 2, 1, 0);
        tile.placeTotem(p1);
        board.addOfferTile(tile);

        Hunter top1 = new Hunter(1, 1, 0, false, 2);
        Hunter top2 = new Hunter(2, 1, 0, false, 2);
        Hunter topExcess = new Hunter(3, 1, 0, false, 2);

        Gatherer bot1 = new Gatherer(20, 1, 0, 2);
        Gatherer botExcess = new Gatherer(22, 1, 0, 2);

        board.addCardToTopRow(top1);
        board.addCardToTopRow(top2);
        board.addCardToTopRow(topExcess);
        board.addCardToBottomRow(bot1);
        board.addCardToBottomRow(botExcess);

        game.getCurrentPhase().startPhase(game);

        // Legal drafts
        assertDoesNotThrow(() -> game.addCard(p1, top1));
        assertDoesNotThrow(() -> game.addCard(p1, bot1));

        // Try over-drafting from bottom
        assertThrows(IllegalStateException.class, () -> game.addCard(p1, botExcess));


        // Final legal top draft
        game.addCard(p1, top2);

        // Try over-drafting from top
        assertThrows(IllegalStateException.class, () -> game.addCard(p1, topExcess));

    }

    @Test
    void testAutoSkipWhenNoCardsAvailable() {
        p1.setCanTakeExtraCard(true); // so FSM next state will be ExtraDraw

        OfferTile tile = new OfferTile('A', 1, 1, 1, 0);
        tile.placeTotem(p1);
        board.addOfferTile(tile);

        // Only an Event in the bottom row (which is undraftable).
        // FSM should realize P1 has nothing to do and auto-skip.
        board.addCardToBottomRow(new Hunt(3001, 1, 0, false, 0));

        game.getCurrentPhase().startPhase(game);

        assertTrue(game.getCurrentPhase() instanceof ExtraDrawState);
        assertNull(tile.getTotem());
    }

    @Test
    void testBuilderDiscount() {
        p1.setCanTakeExtraCard(true);

        OfferTile tile = new OfferTile('A', 1, 1, 0, 0);
        tile.placeTotem(p1);
        board.addOfferTile(tile);

        // P1 has 0 food, but has a Builder (-3 cost)
        p1.addCard(new Builder(5001, 1, 0, 0, 3, 2));
        p1.setFood(0);

        BuildingCard bCard = BuildingFactory.createBuilding(mockBuildingDTO(8001, 2, "ADDCARD", "EFFECT1"));
        board.addCardToTopRow(bCard);


        game.getCurrentPhase().startPhase(game);

        // Should pass since cost (2) is completely offset by the discount (3)
        assertDoesNotThrow(() -> game.addCard(p1, bCard));
        assertTrue(p1.getBuildings().contains(bCard));
    }

    @Test
    void testDraftingSideEffects() {
        OfferTile tile = new OfferTile('A', 1, 1, 0, 0);
        tile.placeTotem(p1);
        board.addOfferTile(tile);

        // Setup for pair tracking
        p1.addCard(new Inventor(4001, 1, 0, Item.ARROW, 2));
        Inventor match = new Inventor(4002, 1, 0, Item.ARROW, 2);
        board.addCardToTopRow(match);

        // Setup for building trigger (grants +1 food when the player's totem is replaced on TurnTile)
        BuildingCard bCard = BuildingFactory.createBuilding(mockBuildingDTO(8, 0, "ONTOTEMREPLACEMENT", "EFFECT10"));
        p1.addCard(bCard);

        p1.setFood(0);

        game.getCurrentPhase().startPhase(game);

        // Draft the matching inventor. This exhausts P1's draws and triggers the end of their turn
        game.addCard(p1, match);

        // Check pair delta tracking
        assertEquals(1, p1.getNewlyFormedInventorPairs());

        // Check if the FSM properly moved P1 to TurnTile and evaluated the effects
        // Space 1 (+1 food) + Building trigger (+1 food) = 2
        assertEquals(2, p1.getFood());
    }
}