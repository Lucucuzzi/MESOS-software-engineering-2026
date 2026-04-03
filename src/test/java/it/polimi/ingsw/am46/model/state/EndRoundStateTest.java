package it.polimi.ingsw.am46.model.state;


import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EndRoundStateTest {
    private Game game;
    private Board board;
    private Player p1, p2;

    @BeforeEach
    void setUp() {
        game = new Game();
        board = game.getBoard();

        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);
    }

    @Test
    void testStandardRoundCleanup() {
        BuildingCard topBuilding = new BuildingCard(101, 1,  0, 0, TriggerType.ADDCARD, null);
        Hunter topHunter = new Hunter(201, 1, 0, false, 2);
        board.getTopRow().add(topBuilding);
        board.getTopRow().add(topHunter);

        BuildingCard bottomBuilding = new BuildingCard(102, 1, 0, 0,  TriggerType.ADDCARD, null);
        Gatherer bottomGatherer = new Gatherer(301, 1, 0, 2);
        board.getBottomRow().add(bottomBuilding);
        board.getBottomRow().add(bottomGatherer);

        for (int i = 0; i < 10; i++) {
            board.getTribeDeck().addCardToTop(new Gatherer(i, 1, 0, 2));
        }

        game.resolveRound();

        // Bottom characters are discarded, buildings stay
        assertTrue(board.getBottomRow().contains(bottomBuilding));
        assertFalse(board.getBottomRow().contains(bottomGatherer));

        // Top characters slide down, buildings stay
        assertTrue(board.getBottomRow().contains(topHunter));
        assertTrue(board.getTopRow().contains(topBuilding));
        assertFalse(board.getTopRow().contains(topHunter));

        assertEquals(2, game.getRound());
    }

    @Test
    void testEraTransition() {
        assertEquals(1, game.getCurrentEra());

        board.getTribeDeck().addCardToTop(new Hunter(98, 2, 0, false, 2));

        game.resolveRound();

        assertEquals(2, game.getCurrentEra());
    }

    @Test

    void testGameOverAndWinner() {
        // Force transitions up to Round 10, Era 3
        board.getTribeDeck().addCardToTop(new Hunter(75, 2, 0, false, 2));
        game.resolveRound();

        board.getTribeDeck().addCardToTop(new Hunter(32, 3, 0, false, 2));
        game.resolveRound();

        for (int i = 3; i <= 9; i++) {
            board.getTribeDeck().addCardToTop(new Hunter(i, 3, 0, false, 2));
            game.resolveRound();
        }

        game.setCurrentPhase(new EndRoundState());
        assertTrue(game.isGameOver());

        p1.modifyPP(10);
        p2.modifyPP(15);

        BuildingCard endBuilding = new BuildingCard(111, 1, 0, 0, TriggerType.ENDTURN, ctx -> {
            ctx.getActivePlayer().modifyPP(25);
        });
        p1.addCard(endBuilding);

        game.countFinalPoints();

        assertEquals(35, p1.getPP());

        List<Player> winners = (List<Player>) game.getWinner();
        assertTrue(winners.contains(p1));
    }

}

