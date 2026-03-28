package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.Builder;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddCardStateTest {

    private Game game;
    private Player player1;
    private Player player2;
    private Board board;
    private AddCardState addCardState;

    @BeforeEach
    void setUp() {
        // 1. Inizializzo il Game vero
        game = new Game();

        // 2. Creo i giocatori e li aggiungo al game
        player1 = new Player("Alice");
        player2 = new Player("Bob");
        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        board = game.getBoard();

        // 3. Setup della Board (OfferTiles)
        // Tile A: Pesca 1 sopra, 0 sotto.
        OfferTile tileA = new OfferTile('A', 1, 0, 1, 0);
        // Tile B: Pesca 0 sopra, 1 sotto.
        OfferTile tileB = new OfferTile('B', 2, 1, 0, 0);

        // Piazziamo i Totem per simulare che la fase PlaceTotem sia finita
        tileA.placeTotem(player1);
        tileB.placeTotem(player2);

        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);

        // 4. Inizializzo lo State vero e lo metto nel Game
        addCardState = new AddCardState();
        game.setCurrentPhase(addCardState);
    }

    @Test
    void testNotActivePlayerThrowsException() {
        // Avvio la fase: lo stato deve settare Player1 (su Tile A) come giocatore attivo
        addCardState.startPhase((GameContext) game);

        Hunter hunter = new Hunter(1, 1, 0, false, 2);
        board.getTopRow().add(hunter);

        // Player2 prova a chiamare l'azione di pescare, ma tocca a Player1
        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player2, hunter);
        }, "Should throw exception if player is not active");
    }

    @Test
    void testNotEnoughFoodThrowsException() {
        addCardState.startPhase((GameContext) game);

        // Building costa 3 cibo. Player1 ha 0 cibo di default.
        BuildingCard expensiveBuilding = new BuildingCard(
                2, 1, 3, 0, 0, TriggerType.ADDCARD, ctx -> {}
        );
        board.getTopRow().add(expensiveBuilding);

        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player1, expensiveBuilding);
        }, "Should throw exception if player doesn't have enough food");
    }

    @Test
    void testBuilderDiscountWorks() {
        addCardState.startPhase((GameContext) game);
        player1.modifyFood(1); // Diamo 1 cibo a Player1

        // Diamo a Player1 un Costruttore che dà 2 di sconto
        Builder builder = new Builder(3, 1, 0, 0, 2, 2);
        player1.getCharacters().add(builder);

        // Building costa 3 cibo. Con lo sconto del builder, il costo scende a 1.
        BuildingCard building = new BuildingCard(
                4, 1, 3, 0, 0, TriggerType.ADDCARD, ctx -> {}
        );
        board.getTopRow().add(building);

        // L'operazione deve completarsi senza lanciare eccezioni
        assertDoesNotThrow(() -> {
            game.addCard(player1, building);
        });

        // Il costo finale (1) deve essere sottratto dal cibo del giocatore
        assertEquals(0, player1.getFood(), "Food should be correctly deducted considering builder discount");
        // L'edificio deve essere nella sua mano
        assertTrue(player1.getBuildings().contains(building), "Building should be added to player");
    }

    @Test
    void testDrawAvailabilityLimits() {
        addCardState.startPhase((GameContext) game); // Player1 ha diritto a 1 pescata dalla TOP Row

        Hunter hunterBottom = new Hunter(5, 1, 0, false, 2);
        board.getBottomRow().add(hunterBottom); // Mettiamo la carta nella Bottom Row

        // Player1 prova a pescare da sotto, ma la sua tessera A permette 0 da sotto
        assertThrows(IllegalStateException.class, () -> {
            game.addCard(player1, hunterBottom);
        }, "Should throw exception if trying to draw from an unauthorized row");
    }

    @Test
    void testTurnAdvancesAutomatically() {
        addCardState.startPhase((GameContext) game); // Player1 è attivo

        Hunter topCard = new Hunter(6, 1, 0, false, 2);
        board.getTopRow().add(topCard);

        // Player1 pesca la sua unica carta disponibile
        game.addCard(player1, topCard);

        // Avendo esaurito le pescate della sua Tile, lo State dovrebbe aver avanzato
        // il turno chiamando ctx.setActivePlayer(prossimoTotem)
        assertEquals(player2, game.getActivePlayer(), "Active player should advance to Bob");
    }

    @Test
    void testInventorPairsDeltaLogic() {
        addCardState.startPhase((GameContext) game);

        // Player1 ha già un Inventore BOAT
        player1.getCharacters().add(new Inventor(7, 1, 0, Item.BOAT, 2));

        // Player 1 deve pescare un altro Inventore BOAT
        Inventor secondInventor = new Inventor(8, 1, 0, Item.BOAT, 2);
        board.getTopRow().add(secondInventor);

        // Pre-condizione
        assertEquals(0, player1.getNewlyFormedInventorPairs());

        // Player 1 pesca la carta
        game.addCard(player1, secondInventor);

        // Post-condizione: la logica delta ha aggiornato i valori
        assertEquals(1, player1.countInventorPairs(), "Player should have 1 pair");
        assertEquals(1, player1.getNewlyFormedInventorPairs(), "Delta newly formed should be 1");
    }
}