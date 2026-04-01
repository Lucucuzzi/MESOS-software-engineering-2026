package it.polimi.ingsw.am46.model;


import it.polimi.ingsw.am46.model.cards.characterCards.Artist;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestThreePlayers {

    private Game game;
    private Board board;
    private Player p1, p2, p3;
    private OfferTile tileA, tileB, tileC;

    @BeforeEach
    void setupThreePlayersGame() throws Exception {
        game = new Game();
        board = game.getBoard();


        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        p3 = new Player("Manuel");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);
        game.getPlayers().add(p3);


        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 2, 0);
        Space s2 = new Space(2, 0, 0);
        Space s3 = new Space(3, 0, 0);

        s1.setPlayer(p1); // Orazio first
        s2.setPlayer(p2); // Luca second
        s3.setPlayer(p3); // Manuel third
        spaces.add(s1);
        spaces.add(s2);
        spaces.add(s3);

        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);


        tileA = new OfferTile('A', 1, 0, 1, 0); // Pesca 1 da Sopra
        tileB = new OfferTile('B', 2, 1, 0, 0); // Pesca 1 da Sotto
        tileC = new OfferTile('C', 3, 0, 1, 0); // Pesca 1 da Sopra

        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);
        board.getOfferTiles().add(tileC);


        p1.modifyFood(50);
        p2.modifyFood(50);
        p3.modifyFood(50);


        for(int i=0; i<15; i++){
            board.getTribeDeck().addCardToTop(new Artist(100+i, 1, 0, 2));
        }

        for(int i=0; i<3; i++){
            board.getTopRow().add(new Gatherer(200+i, 1, 0, 2));
            board.getBottomRow().add(new Hunter(300+i, 1, 0, true, 2));
        }
    }

    @Test
    void testThreePlayersThreeRounds() {
        System.out.println(" START MULTIPLAYER TEST: 3 PLAYERS, 3 ROUNDS ");


        System.out.println("\nROUND 1 - PLACEMENT ");
        game.setCurrentPhase(new PlaceTotemState());
        game.getCurrentPhase().startPhase(game);




        assertEquals(p1, game.getActivePlayer());
        game.moveTotem(p1, tileC); // Orazio goes on C

        assertEquals(p2, game.getActivePlayer());
        game.moveTotem(p2, tileA); // Luca goes on A

        assertEquals(p3, game.getActivePlayer());
        game.moveTotem(p3, tileB); // Manuel goes on B

        System.out.println(" ROUND 1 - DRAFTING ");

        assertEquals(p2, game.getActivePlayer(), "Whoever is on Tile A must play first!");
        game.addCard(p2, board.getTopRow().get(0));

        assertEquals(p3, game.getActivePlayer(), "Whoever is on Tile B must play second!");
        game.addCard(p3, board.getBottomRow().get(0));

        assertEquals(p1, game.getActivePlayer(), "Whoever is on Tile C must play third!");
        game.addCard(p1, board.getTopRow().get(0));



        assertEquals(2, game.getRound(), "We must be arrived at round 2!");


        System.out.println("\n ROUND 2 - PLACEMENT ");


        assertEquals(p2, game.getActivePlayer(), "First must be Luca");
        game.moveTotem(p2, tileB);

        assertEquals(p3, game.getActivePlayer(), "Second must be Manuel");
        game.moveTotem(p3, tileC);

        assertEquals(p1, game.getActivePlayer(), "Third must be Orazio");
        game.moveTotem(p1, tileA);

        System.out.println(" ROUND 2 - DRAFTING ");

        // Tile A: P1 (Orazio)
        assertEquals(p1, game.getActivePlayer(), "Orazio drafts first");
        game.addCard(p1, board.getTopRow().getFirst());

        // Tile B: P2 (Luca)
        assertEquals(p2, game.getActivePlayer(), "Luca drafts second");
        game.addCard(p2, board.getBottomRow().getFirst());

        // Tile C: P3 (Manuel)
        assertEquals(p3, game.getActivePlayer(), "Manuel drafts third");
        game.addCard(p3, board.getTopRow().getFirst());



        assertEquals(3, game.getRound(), "We must be arrived at round 2!");

        System.out.println("\nROUND 3 - PLACEMENT ");

        assertEquals(p1, game.getActivePlayer(), "Orazio first");
        game.moveTotem(p1, tileA);

        assertEquals(p2, game.getActivePlayer(), "Luca  second!");
        game.moveTotem(p2, tileB);

        assertEquals(p3, game.getActivePlayer(), "Manuel third!");
        game.moveTotem(p3, tileC);

        System.out.println("[ ROUND 3 - DRAFTING ]");

        assertEquals(p1, game.getActivePlayer());
        game.addCard(p1, board.getTopRow().getFirst());


        assertEquals(p2, game.getActivePlayer());
        game.addCard(p2, board.getBottomRow().getFirst());


        assertEquals(p3, game.getActivePlayer());

        game.addCard(p3, board.getTopRow().getFirst());

        assertEquals(4, game.getRound(), "Test completed 3 rounds");


        System.out.println("\nTEST COMPLETED");

    }
}