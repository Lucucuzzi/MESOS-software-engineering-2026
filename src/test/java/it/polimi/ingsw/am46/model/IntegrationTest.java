package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingFactory;
import it.polimi.ingsw.am46.model.cards.characterCards.Artist;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.Sustenance;
import it.polimi.ingsw.am46.model.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private Game game;
    private Board board;
    private Player p1, p2;
    private OfferTile tileA, tileB;

    private BuildingCard topBuilding;
    private Gatherer bottomGatherer;

    @BeforeEach
    void setupControllerEnvironment() throws Exception {
        game = new Game();
        board = game.getBoard();

        // 1. IL SERVER CREA I GIOCATORI
        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        game.getPlayers().add(p1);
        game.getPlayers().add(p2);

        // 2. CREAZIONE DELLA TURN TILE E ORDINE INIZIALE
        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 2, 0);  // 1 Spazio: +2 Cibo a fine turno
        Space s2 = new Space(2, -1, 0); // 2 Spazio: -1 Cibo a fine turno

        s1.setPlayer(p1);
        s2.setPlayer(p2);

        spaces.add(s1);
        spaces.add(s2);

        TurnTile turnTile = new TurnTile(spaces);

        // Iniezione manuale per il test
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        // 3. CREAZIONE DELLE OFFER TILES (Tessere Offerta)
        tileA = new OfferTile('A', 1, 0, 1, 0); // 1 Da Sopra
        tileB = new OfferTile('B', 2, 1, 0, 0); // 1 Da Sotto
        board.getOfferTiles().add(tileA);
        board.getOfferTiles().add(tileB);

        // 4. RISORSE DI PARTENZA (Da regolamento Mesos)
        p1.modifyFood(2);
        p2.modifyFood(3);

        // 5. CARTE SUL TABELLONE
        topBuilding = new BuildingCard(101, 1, 2, 0, 0, TriggerType.ENDTURN, ctx -> {
            ctx.getActivePlayer().modifyPP(25);
        });
        Artist topArtist = new Artist(1, 1, 0, 2);
        board.getTopRow().add(topBuilding);
        board.getTopRow().add(topArtist);

        bottomGatherer = new Gatherer(2, 1, 0, 2);
        Sustenance bottomEvent = new Sustenance(3, 1, 0, false, 2);
        board.getBottomRow().add(bottomGatherer);
        board.getBottomRow().add(bottomEvent);

        // Mazzo Tribù: Carta Era 2 (per testare lo scatto di cambio Era)
        Hunter era2Hunter = new Hunter(4, 2, 0, true, 2);
        board.getTribeDeck().addCardToTop(era2Hunter);
    }

    @Test
    void testControllerFullSimulation() {
        System.out.println("=== INIZIO SIMULAZIONE CONTROLLER (2 GIOCATORI) ===");

        // ==========================================
        // AVVIO DELLA PARTITA
        // ==========================================
        RoundPhase initialPhase = new PlaceTotemState();
        game.setCurrentPhase(initialPhase);
        initialPhase.startPhase(game);

        // ==========================================
        // FASE 1: PLACE TOTEM
        // ==========================================
        assertEquals(p1, game.getActivePlayer(), "Il primo giocatore sulla TurnTile (P1) deve essere attivo.");

        // SIMULAZIONE P1: Rimuove dalla TurnTile, Mette sull'Offerta A
        board.getTurnTile().takeTotem(1);
        game.moveTotem(p1, tileA);

        // La FSM passa automaticamente a P2
        assertEquals(p2, game.getActivePlayer(), "Il turno deve passare a P2");

        // SIMULAZIONE P2: Rimuove dalla TurnTile, Mette sull'Offerta B
        board.getTurnTile().takeTotem(2);
        game.moveTotem(p2, tileB);

        // ==========================================
        // FASE 2: ADD CARD (DRAFTING)
        // ==========================================
        assertEquals(p1, game.getActivePlayer(), "P1 sta sulla Tile A, deve draftare per primo.");

        // SIMULAZIONE P1: Compra l'Edificio dalla fila Alta
        game.addCard(p1, topBuilding);

        // VERIFICA ECONOMIA P1
        assertEquals(2, p1.getFood(), "P1 ha pagato 2 cibo (2->0) e preso +2 dallo Spazio 1. Totale 2.");
        assertTrue(p1.getBuildings().contains(topBuilding), "P1 deve avere l'edificio acquistato.");

        // La FSM passa automaticamente a P2
        assertEquals(p2, game.getActivePlayer(), "Ora tocca a P2 che sta sulla Tile B.");

        // SIMULAZIONE P2: Prende il Raccoglitore dalla fila Bassa
        game.addCard(p2, bottomGatherer);

        // VERIFICA ECONOMIA P2
        assertEquals(2, p2.getFood(), "P2 ha pagato 0 cibo e subìto la malus di -1 dello Spazio 2. Totale 2.");
        assertTrue(p2.getCharacters().contains(bottomGatherer), "P2 deve avere il Gatherer.");

        // ==========================================
        // FASE 3: RISOLUZIONE EVENTI E FINE ROUND
        // ==========================================
        System.out.println("Risoluzione automatica Eventi e Round...");

        game.resolveEvents();
        game.resolveRound();

        // VERIFICA EVENTO: Sustenance (Sostentamento)
        // P2 possiede il Gatherer (Sconto: 3). Il costo per un Personaggio è 1.
        // Costo = 0. P2 non paga nulla, e non perde nessun PP.
        assertEquals(2, p2.getFood(), "P2 non ha pagato il cibo evento grazie al Gatherer.");
        assertEquals(0, p2.getPP(), "P2 non ha perso Punti Prestigio.");

        // VERIFICA CAMBIO ERA
        assertEquals(2, game.getCurrentEra(), "L'apparizione della carta Era 2 ha scatenato il cambio Era!");

        long remainingOldCards = board.getBottomRow().stream().filter(c -> c.getType() == Type.CHARACTER || c.getType() == Type.EVENT).count();
        assertEquals(1, remainingOldCards, "L'Artista non draftato deve essere scivolato nella Bottom Row!");

        System.out.println("=== SIMULAZIONE CONTROLLER COMPLETATA CON SUCCESSO ===");
    }
}