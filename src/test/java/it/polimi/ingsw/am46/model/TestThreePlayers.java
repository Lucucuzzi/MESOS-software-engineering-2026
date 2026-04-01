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

        // 1. SETUP DEI 3 GIOCATORI
        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        p3 = new Player("Manuel");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);
        game.getPlayers().add(p3);

        // 2. TURN TILE: SPAZI PER 3 GIOCATORI
        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 2, 0);  // Primo spazio: +2 Cibo
        Space s2 = new Space(2, 0, 0);  // Secondo: neutro
        Space s3 = new Space(3, -1, 0); // Terzo: -1 Cibo

        s1.setPlayer(p1); // Orazio parte primo
        s2.setPlayer(p2); // Luca parte secondo
        s3.setPlayer(p3); // Manuel parte terzo
        spaces.add(s1);
        spaces.add(s2);
        spaces.add(s3);

        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        // 3. TESSERE OFFERTA: 3 TESSERE PER 3 GIOCATORI
        tileA = new OfferTile('A', 1, 0, 1, 0); // Pesca 1 da Sopra
        tileB = new OfferTile('B', 2, 1, 0, 0); // Pesca 1 da Sotto
        tileC = new OfferTile('C', 3, 0, 1, 0); // Pesca 1 da Sopra

        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);
        board.getOfferTiles().add(tileC);

        // Tutti molto ricchi di cibo per bypassare check di povertà in AddCard
        p1.modifyFood(50);
        p2.modifyFood(50);
        p3.modifyFood(50);

        // 4. MAZZO TRIBÙ E CARTE DELLA PLANCIA
        // Riempiamo le righe con tantissimi dummy così nessuno rimane a secco
        for(int i=0; i<15; i++){
            board.getTribeDeck().addCardToTop(new Artist(100+i, 1, 0, 2));
        }

        // Mettiamo alcune carte fisicamente in riga Top e Bottom per il primo round
        for(int i=0; i<3; i++){
            board.getTopRow().add(new Gatherer(200+i, 1, 0, 2));
            board.getBottomRow().add(new Hunter(300+i, 1, 0, true, 2));
        }
    }

    @Test
    void testThreePlayersThreeRounds() {
        System.out.println("=== START MULTIPLAYER TEST: 3 PLAYERS, 3 ROUNDS ===");

        // =====================================================================
        // ROUND 1
        // =====================================================================
        System.out.println("\n[ ROUND 1 - PIAZZAMENTO ]");
        game.setCurrentPhase(new PlaceTotemState());
        game.getCurrentPhase().startPhase(game);

        assertEquals(1, game.getRound(), "Dobbiamo essere al round 1!");

        // Iniziano nell'ordine in cui li abbiamo messi sulla TurnTile (P1 -> P2 -> P3)
        assertEquals(p1, game.getActivePlayer());
        game.moveTotem(p1, tileC); // Orazio va su C

        assertEquals(p2, game.getActivePlayer());
        game.moveTotem(p2, tileA); // Luca va su A

        assertEquals(p3, game.getActivePlayer());
        game.moveTotem(p3, tileB); // Manuel va su B

        System.out.println("[ ROUND 1 - DRAFTING ]");
        // Chi drafterà per primo? Chi si trova su Tile A! Quindi P2 (Luca).
        assertEquals(p2, game.getActivePlayer(), "Chi è su Tile A deve giocare per primo!");
        game.addCard(p2, board.getTopRow().get(0));

        // Poi chi è su Tile B: P3 (Manuel)
        assertEquals(p3, game.getActivePlayer(), "Chi è su Tile B deve giocare per secondo!");
        game.addCard(p3, board.getBottomRow().get(0));

        // Infine chi è su Tile C: P1 (Orazio)
        assertEquals(p1, game.getActivePlayer(), "Chi è su Tile C deve giocare per ultimo!");
        game.addCard(p1, board.getTopRow().get(0));

        // La FSM fa scattare l'EndRoundState e passa in automatico al Round 2!

        assertEquals(2, game.getRound(), "Dobbiamo essere al round 2!");

        // =====================================================================
        // ROUND 2
        // =====================================================================
        System.out.println("\n[ ROUND 2 - PIAZZAMENTO ]");

        // Nel round 1, l'ordine di fine draft (chi esaurisce le mosse per primo)
        // stabilisce l'ordine in TurnTile per il round 2!
        // L'ordine in cui hanno finito il draft è: Luca -> Manuel -> Orazio
        assertEquals(p2, game.getActivePlayer(), "Nel round 2 il primo deve essere Luca (P2)!");
        game.moveTotem(p2, tileB);

        assertEquals(p3, game.getActivePlayer(), "Il secondo deve essere Manuel (P3)!");
        game.moveTotem(p3, tileC);

        assertEquals(p1, game.getActivePlayer(), "L'ultimo deve essere Orazio (P1)!");
        game.moveTotem(p1, tileA);

        System.out.println("[ ROUND 2 - DRAFTING ]");
        // Ora l'ordine di draft sarà stabilito di nuovo dalle tessere:
        // Tile A: P1 (Orazio)
        assertEquals(p1, game.getActivePlayer(), "Chi è su Tile A (Orazio) drafta per primo!");
        game.addCard(p1, board.getTopRow().get(0));

        // Tile B: P2 (Luca)
        assertEquals(p2, game.getActivePlayer(), "Chi è su Tile B (Luca) drafta per secondo!");
        game.addCard(p2, board.getBottomRow().get(0));

        // Tile C: P3 (Manuel)
        assertEquals(p3, game.getActivePlayer(), "Chi è su Tile C (Manuel) drafta per ultimo!");
        game.addCard(p3, board.getTopRow().get(0));

        // La FSM avanza da sola al Round 3!

        assertEquals(3, game.getRound(), "Dobbiamo essere al round 3!");

        // =====================================================================
        // ROUND 3
        // =====================================================================
        System.out.println("\n[ ROUND 3 - PIAZZAMENTO ]");

        // L'ordine di fine draft del Round 2 era: Orazio -> Luca -> Manuel.
        assertEquals(p1, game.getActivePlayer(), "Nel round 3 Orazio torna ad essere il primo!");
        game.moveTotem(p1, tileA);

        assertEquals(p2, game.getActivePlayer(), "Luca è il secondo!");
        game.moveTotem(p2, tileB);

        assertEquals(p3, game.getActivePlayer(), "Manuel è il terzo!");
        game.moveTotem(p3, tileC);

        System.out.println("[ ROUND 3 - DRAFTING ]");
        // Chi c'è su Tile A? P1!
        assertEquals(p1, game.getActivePlayer());
        game.addCard(p1, board.getTopRow().get(0));

        // Tile B? P2!
        assertEquals(p2, game.getActivePlayer());
        game.addCard(p2, board.getBottomRow().get(0));

        // Tile C? P3!
        assertEquals(p3, game.getActivePlayer());
        // Questa ultima carta farà avanzare il Round a 4!
        game.addCard(p3, board.getTopRow().get(0));

        assertEquals(4, game.getRound(), "Il test ha completato 3 round interi e siamo giunti all'inizio del quarto!");


        System.out.println("\n=== TEST SUPERATO CON SUCCESSO! ===");
        System.out.println("La Macchina a Stati ruota perfettamente 3 giocatori e calcola sempre il giusto ordine di Plancia e di Turno.");
    }
}