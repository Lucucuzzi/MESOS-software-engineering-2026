package it.polimi.ingsw.am46.model;


import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.eventCards.*;
import it.polimi.ingsw.am46.model.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestEventFSMRules {

    private Game game;
    private Board board;
    private Player p1, p2, p3;
    private OfferTile tileA, tileB, tileC;

    @BeforeEach
    void setupGame() throws Exception {
        game = new Game();
        board = game.getBoard();

        // 3 Giocatori
        p1 = new Player("Orazio");
        p2 = new Player("Luca");
        p3 = new Player("Riccardo");
        game.getPlayers().addAll(List.of(p1, p2, p3));

        // TurnTile (Neutrale per non falsare i conti del cibo)
        List<Space> spaces = new ArrayList<>();
        Space s1 = new Space(1, 0, 0);
        Space s2 = new Space(2, 0, 0);
        Space s3 = new Space(3, 0, 0);
        s1.setPlayer(p1);
        s2.setPlayer(p2);
        s3.setPlayer(p3);
        spaces.addAll(List.of(s1, s2, s3));

        TurnTile turnTile = new TurnTile(spaces);
        Field turnTileField = Board.class.getDeclaredField("turnTile");
        turnTileField.setAccessible(true);
        turnTileField.set(board, turnTile);

        // OfferTiles (Tutte da 1 Sopra, per semplicità e precisione di draft)
        tileA = new OfferTile('A', 1, 0, 1, 0);
        tileB = new OfferTile('B', 2, 0, 1, 0);
        tileC = new OfferTile('C', 3, 0, 1, 0);
        board.getOfferTiles().addAll(List.of(tileA, tileB, tileC));

        // Risorse Iniziali
        p1.modifyFood(20);
        p2.modifyFood(20);
        p3.modifyFood(20);

        p1.modifyPP(10);
        p2.modifyPP(10);
        p3.modifyPP(10);
    }

    @Test
    void testDueRoundEventiEConteggioFinale() throws Exception {
        System.out.println("=== TEST EVENTI E PUNTEGGI MESOS (2 ROUNDS) ===");

        // ==========================================
        // SETUP ROUND 1
        // ==========================================
        // Carte Draftabili Top Row:
        // P1: Edificio che dona l'Immunità allo Sciamano
        BuildingCard bShamanImmunity = new BuildingCard(101, 1, 0, 5, 0, TriggerType.ADDCARD, ctx -> {
            ctx.getActivePlayer().setShamanImmunity(true);
        });
        // P2: Sciamano (2 icone)
        Shaman shamanCard = new Shaman(102, 1, 0, 2, 2);
        // P3: Edificio che dona Bonus durante la Caccia (Trigger ONEVENT)
        BuildingCard bHuntBonus = new BuildingCard(103, 1, 0, 0, 0, TriggerType.ONEVENT, ctx -> {
            if (ctx.getCurrentEvent() != null && ctx.getCurrentEvent().getSubType() == SubType.HUNT) {
                Player p = ctx.getActivePlayer();
                int hunters = p.countCharactersByType(SubType.HUNTER);
                p.modifyFood(hunters); // +1 Cibo per cacciatore extra
                p.modifyPP(hunters);   // +1 PP per cacciatore extra
            }
        });

        board.getTopRow().addAll(List.of(bShamanImmunity, shamanCard, bHuntBonus));

        // Eventi Bottom Row (Risolti a fine round 1):
        EventCard shamanRitual = new ShamanicRitual(801, 1, 0, false, 5, 3); // Vince 5, Perde 3
        EventCard huntEvent = new Hunt(802, 1, 0, false, 2); // 2 PP per ogni Cacciatore
        board.getBottomRow().addAll(List.of(shamanRitual, huntEvent));

        // START ROUND 1
        game.setCurrentPhase(new PlaceTotemState());
        game.getCurrentPhase().startPhase(game);

        game.moveTotem(p1, tileA); // P1 prende l'immunità
        game.moveTotem(p2, tileB); // P2 prende lo sciamano
        game.moveTotem(p3, tileC); // P3 prende il bonus caccia

        game.addCard(p1, bShamanImmunity);
        game.addCard(p2, shamanCard);
        game.addCard(p3, bHuntBonus);

        // FINE ROUND 1 -> RISOLUZIONE EVENTI
        // 1. Shamanic Ritual:
        // P2 ha 2 icone (MAX) -> Vince 5 PP. (10 -> 15)
        // P3 ha 0 icone (MIN) -> Perde 3 PP. (10 -> 7)
        // P1 ha 0 icone (MIN) ma HA L'IMMUNITÀ! -> Non perde nulla! (Resta 10)
        assertEquals(10, p1.getPP(), "P1 doveva essere immune al Rituale Sciamanico!");
        assertEquals(15, p2.getPP(), "P2 doveva vincere il Rituale!");
        assertEquals(7, p3.getPP(), "P3 doveva perdere PP al Rituale!");

        // 2. Hunt: Nessuno ha cacciatori, l'evento non fa nulla.
        // P3 ha l'edificio ma 0 cacciatori, quindi bonus = 0.
        assertEquals(20, p3.getFood(), "P3 non ha cacciatori, non prende cibo!");

        // ==========================================
        // SETUP ROUND 2
        // ==========================================
        board.getTopRow().clear();
        board.getBottomRow().clear();

        // Carte Draftabili Top Row:
        Inventor inventorCard = new Inventor(201, 2, 0, Item.BOAT, 2);
        Artist artistCard = new Artist(202, 2, 0, 2);
        Hunter hunterCard = new Hunter(203, 2, 0, false, 2);

        board.getTopRow().addAll(List.of(inventorCard, artistCard, hunterCard));

        // Eventi Bottom Row (Risolti a fine round 2):
        EventCard cavePaintings = new CavePaintings(803, 2, 0, false, 1, 2, 3); // Min 1 artista, penale 2, premio 3
        EventCard sustenance = new Sustenance(804, 2, 0, false, 4); // 4 PP di penale per ogni unfed
        board.getBottomRow().addAll(List.of(cavePaintings, sustenance));

        // L'ordine è rimasto P1, P2, P3
        game.moveTotem(p1, tileA); // P1 prende l'Inventore
        game.moveTotem(p2, tileB); // P2 prende l'Artista
        game.moveTotem(p3, tileC); // P3 prende il Cacciatore

        game.addCard(p1, inventorCard);
        game.addCard(p2, artistCard);
        game.addCard(p3, hunterCard);

        // FINE ROUND 2 -> RISOLUZIONE EVENTI
        // 1. Cave Paintings:
        // P2 ha 1 Artista (>= 1). Riceve 1 * 3 = 3 PP. (15 -> 18)
        // P1 ha 0 Artisti. Perde 2 PP. (10 -> 8)
        // P3 ha 0 Artisti. Perde 2 PP. (7 -> 5)
        assertEquals(8, p1.getPP(), "P1 doveva perdere 2 PP per Pitture Rupestri!");
        assertEquals(18, p2.getPP(), "P2 doveva guadagnare 3 PP per il suo Artista!");
        assertEquals(5, p3.getPP(), "P3 doveva perdere 2 PP per Pitture Rupestri!");

        // 2. Sustenance: Tutti sfamano.
        // P1 (1 pers). 20 - 1 = 19 Cibo.
        // P2 (2 pers). 20 - 2 = 18 Cibo.
        // P3 (1 pers). 20 - 1 = 19 Cibo.
        assertEquals(19, p1.getFood(), "P1 ha sfamato 1 persona.");
        assertEquals(18, p2.getFood(), "P2 ha sfamato 2 persone.");
        assertEquals(19, p3.getFood(), "P3 ha sfamato 1 persona.");

        // ==========================================
        // FINE PARTITA E CONTEGGIO FINALE (Regole MESOS)
        // ==========================================
        // Forziamo lo stato in modo che il trigger ENDTURN sia coerente per futuri edifici (anche se qui non ne abbiamo)
        game.setCurrentPhase(new EndRoundState());
        game.countFinalPoints();

        // Ricalcoliamo i punteggi:
        // P1:
        // Base: 8 PP
        // Edifici: bShamanImmunity (+5 PP stampati sulla carta).
        // Inventori: 1 Inventore * 1 icona BOAT = +1 PP.
        // Totale P1 = 8 + 5 + 1 = 14 PP.
        assertEquals(14, p1.getPP(), "Conteggio finale P1 errato!");

        // P2:
        // Base: 18 PP
        // Artisti bonus: Ha 1 artista. (1 / 2) * 10 = 0 PP.
        // Totale P2 = 18 PP.
        assertEquals(18, p2.getPP(), "Conteggio finale P2 errato!");

        // P3:
        // Base: 5 PP
        // Nessun bonus di fine partita applicabile.
        // Totale P3 = 5 PP.
        assertEquals(5, p3.getPP(), "Conteggio finale P3 errato!");

        // VERIFICA DEL VINCITORE!
        // Vince P2 con 18 PP!
        List<Player> winners = game.getWinner();
        assertEquals(1, winners.size(), "Ci deve essere un solo vincitore!");
        assertEquals(p2, winners.getFirst(), "P2 (Luca) deve aver vinto la partita!");

        System.out.println("=== TEST SUPERATO: EVENTI, EDIFICI, ED ENDGAME PERFETTI! ===");
    }
}
