package it.polimi.ingsw.am46.model.cards.buildingCards;


import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.CardDataDTO;
import it.polimi.ingsw.am46.model.cards.characterCards.Artist;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.CavePaintings;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import it.polimi.ingsw.am46.model.cards.eventCards.ShamanicRitual;
import it.polimi.ingsw.am46.model.cards.eventCards.Sustenance;
import it.polimi.ingsw.am46.model.state.RoundPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {
    private Player player;
    private TestGameContext ctx;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer");
        List<Player> players = new ArrayList<>();
        players.add(player);

        ctx = new TestGameContext(players);
        ctx.setCurrentPlayer(player);
    }

    private BuildingCard createTestBuilding(int id, int era, int cost, int pp, String trigger, String effect) {
        CardDataDTO.BuildingDTO dto = new CardDataDTO.BuildingDTO();
        dto.id = id;
        dto.era = era;
        dto.cost = cost;
        dto.pp = pp;
        dto.triggerType = trigger;
        dto.EffectID = effect;
        return BuildingFactory.createBuilding(dto);
    }

    @Test
    void testSustenanceDiscountsCumulative() {
        player.addCard(new Artist(1, 1, 0, 2));
        player.addCard(new Artist(2, 1, 0, 2));
        player.addCard(new Inventor(3, 1, 0, Item.ARROW, 2));
        player.addCard(new Gatherer(4, 1, 0, 2));

        BuildingCard eff2 = createTestBuilding(1, 1, 0, 0, "ONEVENT", "EFFECT2");
        BuildingCard eff20 = createTestBuilding(2, 1, 0, 0, "ONEVENT", "EFFECT20");
        BuildingCard eff21 = createTestBuilding(3, 1, 0, 0, "ONEVENT", "EFFECT21");

        ctx.setCurrentEvent(new Hunt(99, 1, 0, false, 0));
        eff2.applyEffect(ctx);
        assertEquals(0, player.getSustenanceDiscount(), "Non deve applicarsi se l'evento non è Sostentamento");

        ctx.setCurrentEvent(new Sustenance(100, 1, 0, false, 2));

        eff2.applyEffect(ctx);   // +2 sconto (Artisti)
        eff20.applyEffect(ctx);  // +1 sconto (Inventori)
        eff21.applyEffect(ctx);  // +1 sconto (Gatherers)

        assertEquals(4, player.getSustenanceDiscount(), "Total discount must be 4");
    }

    @Test
    void testDynamicFoodAndFlags() {

        BuildingCard eff3 = createTestBuilding(1, 1, 0, 0, "ADDCARD", "EFFECT3");
        player.setNewlyFormedInventorPairs(2);
        eff3.applyEffect(ctx);
        assertEquals(6, player.getFood());
        player.resetNewlyFormedInventorPairs();

        BuildingCard eff4 = createTestBuilding(11, 1, 0, 0, "ONEVENT", "EFFECT4");
        player.addCard(new Hunter(12, 1, 0, false, 2));
        ctx.setCurrentEvent(new Hunt(101, 1, 0, false, 0)); // Setto l'evento caccia
        eff4.applyEffect(ctx);
        assertEquals(7, player.getFood()); // 6 + 1
        assertEquals(1, player.getPP());   // 0 + 1

        // Test EFFECT 8 (Pitture Rupestri)
        BuildingCard eff8 = createTestBuilding(13, 1, 0, 0,  "ONEVENT", "EFFECT8");
        player.addCard(new Artist(14, 1, 0, 2));
        ctx.setCurrentEvent(new CavePaintings(103, 1, 0, false, 1, 0, 0));
        eff8.applyEffect(ctx);
        assertEquals(8, player.getFood()); // 7 + 1

        BuildingCard eff9 = createTestBuilding(2, 1, 0, 0,  "ADDCARD", "EFFECT9");
        player.setNewlyFormedSets(1);
        eff9.applyEffect(ctx);
        assertEquals(13, player.getFood()); // 8 + 5

        BuildingCard eff11 = createTestBuilding(3, 1, 0, 0, "ONEVENT", "EFFECT11");
        eff11.applyEffect(ctx);
        assertTrue(player.canTakeExtraCard());

        Space bonusSpace = new Space(1, 2, 0);
        bonusSpace.setPlayer(player);
        List<Space> spaces = new ArrayList<>();
        spaces.add(bonusSpace);
        TurnTile turnTile = new TurnTile(spaces);
        Board board = new Board() {
            @Override
            public TurnTile getTurnTile() { return turnTile; }
        };
        ctx.setBoard(board);

        BuildingCard eff10 = createTestBuilding(4, 1, 0, 0,  "ONTOTEMPLACEMENT", "EFFECT10");

        eff10.applyEffect(ctx);
        assertEquals(14, player.getFood()); // 13 + 1
    }

    @Test
    void testShamanFlagsCumulative() {

        BuildingCard eff5 = createTestBuilding(1, 1, 0, 0, "ONEVENT", "EFFECT5");
        BuildingCard eff6 = createTestBuilding(2, 1, 0, 0,  "ONEVENT", "EFFECT6");
        BuildingCard eff7 = createTestBuilding(3, 1, 0, 0,  "ONEVENT", "EFFECT7");

        // Impostiamo l'evento a Shamanic Ritual per soddisfare l'IF delle lambda
        ctx.setCurrentEvent(new ShamanicRitual(102, 1, 0, false, 0, 0));

        eff5.applyEffect(ctx);
        eff6.applyEffect(ctx);
        eff7.applyEffect(ctx);

        assertTrue(player.hasShamanImmunity());
        assertTrue(player.hasShamanDoublePP());
        assertEquals(3, player.getExtraShamanIcons());
    }

    private static class MockPhase extends RoundPhase {
        MockPhase(TriggerType triggerType) {
            super(triggerType);
        }
    }
}