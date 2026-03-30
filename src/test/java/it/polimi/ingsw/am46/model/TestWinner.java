package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
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

public class TestWinner {

    private Game game;
    private Board board;
    private Player p1, p2;
    private OfferTile tileA, tileB;

    private BuildingCard topBuildingR1;
    private Artist topArtistR1;
    private Gatherer bottomGathererR1;

    private Hunter era2Hunter;
    private Hunter era3Hunter;

    @BeforeEach
    void setupMiniGame() throws Exception {
        game = new Game();
        board = game.getBoard();

        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);

        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 2, 0);
        Space s2 = new Space(2, -1, 0);

        s1.setPlayer(p1);
        s2.setPlayer(p2);
        spaces.add(s1);
        spaces.add(s2);

        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        tileA = new OfferTile('A', 1, 0, 1, 0); // 1 da Sopra
        tileB = new OfferTile('B', 2, 1, 0, 0); // 1 da Sotto
        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);

        p1.modifyFood(50);
        p2.modifyFood(50);

        topBuildingR1 = new BuildingCard(101, 1, 2, 0, 0, TriggerType.ENDTURN, ctx -> {
            ctx.getActivePlayer().modifyPP(5);
        });
        topArtistR1 = new Artist(1, 1, 0, 2);
        bottomGathererR1 = new Gatherer(2, 1, 0, 2);

        board.getTopRow().add(topBuildingR1);
        board.getTopRow().add(topArtistR1);
        board.getBottomRow().add(bottomGathererR1);

        era3Hunter = new Hunter(301, 3, 0, true, 2);
        era2Hunter = new Hunter(201, 2, 0, true, 2);
        board.getTribeDeck().addCardToTop(era3Hunter);
        board.getTribeDeck().addCardToTop(era2Hunter);
    }

    @Test
    void testFullGameFlowAndWinner() {
        // ROUND 1
        game.setCurrentPhase(new PlaceTotemState());
        game.getCurrentPhase().startPhase(game);

        Player piaz1 = game.getActivePlayer();
        game.moveTotem(piaz1, tileA);

        Player piaz2 = game.getActivePlayer();
        game.moveTotem(piaz2, tileB);

        Player draft1 = game.getActivePlayer();
        game.addCard(draft1, topBuildingR1);

        Player draft2 = game.getActivePlayer();
        game.addCard(draft2, bottomGathererR1);

        // ROUND 2

        // SALVAVITA ORAZIO: Aggiungiamo forzatamente una carta in Bottom Row
        // per evitare che l'AddCardState "skippi" chi sceglie la Tile B.
        board.getBottomRow().add(new Gatherer(999, 1, 0, 2));

        Player piaz1_R2 = game.getActivePlayer();
        game.moveTotem(piaz1_R2, tileB);

        Player piaz2_R2 = game.getActivePlayer();
        game.moveTotem(piaz2_R2, tileA);

        // Draft
        Player drafter1_R2 = game.getActivePlayer();
        if (drafter1_R2 == piaz2_R2) {
            game.addCard(drafter1_R2, board.getTopRow().get(0));
            Player drafter2_R2 = game.getActivePlayer();
            game.addCard(drafter2_R2, board.getBottomRow().get(0));
        } else {
            game.addCard(drafter1_R2, board.getBottomRow().get(0));
            Player drafter2_R2 = game.getActivePlayer();
            game.addCard(drafter2_R2, board.getTopRow().get(0));
        }

        // CALCOLO VINCITORE
        System.out.println("=== CALCOLO VINCITORE ===");

        // Simulo punteggio per pareggio fittizio
        p1.modifyPP(20);
        p2.modifyPP(20);

        // IL GIOCO FINISCE E CALCOLA I PUNTI NELLA FASE "END ROUND STATE"!
        // Forziamo lo stato in modo che le carte che si attivano "ENDTURN" capiscano che è finita la partita.
        game.setCurrentPhase(new EndRoundState());

        // Chiamo la EndGame che farà triggerare l'edificio
        game.countFinalPoints();

        Player playerWithBuilding = (p1.getBuildings().contains(topBuildingR1)) ? p1 : p2;
        Player otherPlayer = (playerWithBuilding == p1) ? p2 : p1;

        assertEquals(25, playerWithBuilding.getPP(), "Il possessore dell'edificio deve aver preso i suoi +5 PP!");
        assertEquals(20, otherPlayer.getPP());
        assertEquals(playerWithBuilding, game.getWinner());
    }
}