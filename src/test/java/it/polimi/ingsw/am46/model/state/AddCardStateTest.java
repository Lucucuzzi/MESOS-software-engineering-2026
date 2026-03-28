package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.Board;
import it.polimi.ingsw.am46.model.OfferTile;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TestGameContext;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingFactory;
import it.polimi.ingsw.am46.model.cards.characterCards.Builder;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddCardStateTest {

    private TestGameContext ctx;
    private Board board;
    private AddCardState state;

    private Player p1;
    private Player p2;
    private Player p3;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p3 = new Player("Charlie");

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);

        ctx = new TestGameContext(players);
        board = new Board();
        ctx.setBoard(board);

        // Setup Offer Track:
        // Tile A (P1): 0 Bottom, 2 Top
        OfferTile tileA = new OfferTile('A', 1, 0, 2, 0);
        // Tile B (P2): 1 Bottom, 1 Top
        OfferTile tileB = new OfferTile('B', 2, 1, 1, 0);
        // Tile C (P3): 1 Bottom, 0 Top
        OfferTile tileC = new OfferTile('C', 3, 1, 0, 0);

        tileA.placeTotem(p1);
        tileB.placeTotem(p2);
        tileC.placeTotem(p3);

        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);
        board.getOfferTiles().add(tileC);

        state = new AddCardState();
        ctx.setCurrentPhase(state);
    }

    @Test
    void testCompleteAddCardPhaseFlow() {
        // --- BOARD CARDS SETUP ---
        BuildingCard expensiveBuilding = BuildingFactory.createBuilding(1, 1, 3, 0, 0, TriggerType.ADDCARD, EffectID.EFFECT1);
        Inventor inventorTop = new Inventor(2, 1, 0, Item.ARROW, 2);
        Hunt eventCard = new Hunt(3, 1, 0, false, 2);
        Hunter basicTopCard = new Hunter(4, 1, 0, false, 2);

        board.getTopRow().add(expensiveBuilding);
        board.getTopRow().add(inventorTop);
        board.getTopRow().add(eventCard);
        board.getTopRow().add(basicTopCard);

        Hunter bottomCard1 = new Hunter(5, 1, 0, false, 2);
        Hunter bottomCard2 = new Hunter(6, 1, 0, false, 2);
        board.getBottomRow().add(bottomCard1);
        board.getBottomRow().add(bottomCard2);

        // --- PLAYERS SETUP ---
        p1.modifyFood(1);
        // Give P1 a Builder for a 2-food discount
        p1.addCard(new Builder(10, 1, 0, 0, 2, 2));
        // Give P1 a basic Inventor
        p1.addCard(new Inventor(11, 1, 0, Item.ARROW, 2));
        // Give P1 an EFFECT3 Building (+3 Food per new inventor pair)
        BuildingCard eff3 = BuildingFactory.createBuilding(12, 1, 0, 0, 0, TriggerType.ADDCARD, EffectID.EFFECT3);
        p1.addCard(eff3);


        // --- PHASE EXECUTION ---

        // 1. Init phase
        state.startPhase(ctx);
        assertEquals(p1, ctx.getActivePlayer(), "P1 should be the first active player based on OfferTrack");

        // 2. Turn protection: P2 cannot act
        assertThrows(IllegalStateException.class, () -> {
            state.handleAddCard(ctx, p2, basicTopCard);
        }, "P2 cannot act during P1's turn");

        // 3. Event cards block
        assertThrows(IllegalStateException.class, () -> {
            state.handleAddCard(ctx, p1, eventCard);
        }, "Players cannot draw Event cards from the board");

        // 4. Row limits validation
        assertThrows(IllegalStateException.class, () -> {
            state.handleAddCard(ctx, p1, bottomCard1);
        }, "P1 cannot draw from bottom row (Tile limits: 0 bottom)");

        // 5. Purchase with Discount (P1's first draw)
        // Building costs 3. Discount is 2. P1 has 1 food.
        assertDoesNotThrow(() -> {
            state.handleAddCard(ctx, p1, expensiveBuilding);
        });
        assertEquals(0, p1.getFood(), "P1 spent their food considering the builder discount");
        assertTrue(p1.getBuildings().contains(expensiveBuilding));
        assertEquals(p1, ctx.getActivePlayer(), "P1 should take another turn due to 2 top draws allowed");

        // 6. Delta Logic & Building Triggers (P1's second draw)
        // P1 draws another ARROW Inventor, forming a pair.
        state.handleAddCard(ctx, p1, inventorTop);
        assertEquals(1, p1.countInventorPairs(), "P1 formed 1 inventor pair");
        assertEquals(3, p1.getFood(), "EFFECT3 building triggered properly, granting 3 food");

        // 7. Auto-advance to next player
        assertEquals(p2, ctx.getActivePlayer(), "Turn automatically passed to P2");

        // 8. Insufficient funds check on P2
        BuildingCard p2TooExpensive = BuildingFactory.createBuilding(99, 1, 3, 0, 0, TriggerType.ADDCARD, EffectID.EFFECT1);
        board.getTopRow().add(p2TooExpensive);

        assertThrows(IllegalStateException.class, () -> {
            state.handleAddCard(ctx, p2, p2TooExpensive);
        }, "P2 cannot afford the building (0 food, 0 discount)");

        // 9. Standard P2 draws (1 Top, 1 Bottom)
        state.handleAddCard(ctx, p2, basicTopCard);
        assertEquals(p2, ctx.getActivePlayer());
        state.handleAddCard(ctx, p2, bottomCard1);

        // 10. Auto-advance to P3
        assertEquals(p3, ctx.getActivePlayer(), "Turn passed to P3");

        // 11. P3's draw (0 Top, 1 Bottom) and Phase termination
        state.handleAddCard(ctx, p3, bottomCard2);


    }
}