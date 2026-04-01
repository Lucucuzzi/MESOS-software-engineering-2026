package it.polimi.ingsw.am46.model.state;


import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.Builder;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddCardStateTest {

    private Game game;
    private Player player1;
    private Player player2;
    private Board board;
    private AddCardState addCardState;

    @BeforeEach
    void setUp() throws Exception {
        game = new Game();
        player1 = new Player("Alice");
        player2 = new Player("Bob");
        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        board = game.getBoard();

        // Tile con 1 pescata Top e 1 pescata Bottom, per evitare che i turni vengano saltati in automatico!
        OfferTile tileA = new OfferTile('A', 1, 1, 1, 0);
        OfferTile tileB = new OfferTile('B', 2, 1, 1, 0);

        tileA.placeTotem(player1);
        tileB.placeTotem(player2);

        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);

        // TurnTile (per non lanciare NullPointerException a fine turno)
        List<Space> spaces = new ArrayList<>();
        spaces.add(new Space(1, 0, 0));
        spaces.add(new Space(2, 0, 0));
        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        // Popoliamo la plancia PRIMA di avviare la fase, altrimenti il giocatore subisce l'auto-skip per assenza di carte!
        board.getTopRow().add(new Hunter(1, 1, 0, false, 2));
        board.getTopRow().add(new Hunter(2, 1, 0, false, 2));
        board.getBottomRow().add(new Hunter(3, 1, 0, false, 2));
        board.getBottomRow().add(new Hunter(4, 1, 0, false, 2));

        addCardState = new AddCardState();
        game.setCurrentPhase(addCardState);
    }

    @Test
    void testNotActivePlayerThrowsException() {
        addCardState.startPhase(game);

        Card cardOnBoard = board.getTopRow().get(0);

        // Tocca ad Alice (player1), quindi Bob (player2) non può pescare
        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player2, cardOnBoard);
        }, "Should throw exception if player is not active");
    }

    @Test
    void testEventCardDrawThrowsException() {
        Hunt huntEvent = new Hunt(10, 1, 0, false, 2);
        board.getBottomRow().add(huntEvent);

        addCardState.startPhase(game);

        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player1, huntEvent);
        }, "Should throw exception when trying to draw an event card");
    }

    @Test
    void testNotEnoughFoodThrowsException() {
        BuildingCard expensiveBuilding = new BuildingCard(11, 1, 3, 0, 0, TriggerType.ADDCARD, ctx -> {});
        board.getTopRow().add(expensiveBuilding);

        // Assicuriamoci che abbia 0 cibo
        player1.modifyFood(0);

        addCardState.startPhase(game);

        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player1, expensiveBuilding);
        }, "Should throw exception if player doesn't have enough food");
    }

    @Test
    void testBuilderDiscountWorks() {
        BuildingCard building = new BuildingCard(12, 1, 3, 0, 0, TriggerType.ADDCARD, ctx -> {});
        board.getTopRow().add(building);

        Builder builder = new Builder(13, 1, 0, 0, 2, 2); // Sconto di 2
        player1.getCharacters().add(builder);

        player1.modifyFood(1); // Ha 1 cibo. Il costo è 3 - 2(sconto) = 1. Può permetterselo!

        addCardState.startPhase(game);

        assertDoesNotThrow(() -> {
            game.addCard(player1, building);
        });

        assertEquals(0, player1.getFood(), "Player should have exactly 0 food after discount");
    }

    @Test
    void testTurnAdvancesAutomatically() {
        addCardState.startPhase(game);

        assertEquals(player1, game.getActivePlayer());

        Card topCard = board.getTopRow().get(0);
        Card bottomCard = board.getBottomRow().get(0);

        // Player1 ha 1 limite Top e 1 limite Bottom per la tileA.
        game.addCard(player1, topCard);

        // Pescando anche la Bottom, i suoi limiti arrivano a 0 -> Il turno passa automaticamente a Player2.
        game.addCard(player1, bottomCard);

        assertEquals(player2, game.getActivePlayer(), "The turn should have automatically advanced to Player 2");
    }

    @Test
    void testInventorPairsDeltaLogic() {
        Inventor inv1 = new Inventor(10, 1, 0, Item.BOAT, 2);
        Inventor inv2 = new Inventor(11, 1, 0, Item.BOAT, 2);

        player1.addCard(inv1); // Ha già metà coppia
        board.getTopRow().add(inv2);

        addCardState.startPhase(game);

        game.addCard(player1, inv2); // Pesca l'altra metà

        assertEquals(1, player1.getNewlyFormedInventorPairs(), "Logic should calculate 1 newly formed pair");
    }
}