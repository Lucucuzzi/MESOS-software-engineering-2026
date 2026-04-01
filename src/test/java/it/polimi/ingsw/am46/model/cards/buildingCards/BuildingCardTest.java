package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.*;
import it.polimi.ingsw.am46.model.cards.characterCards.Artist;
import it.polimi.ingsw.am46.model.cards.characterCards.Gatherer;
import it.polimi.ingsw.am46.model.cards.characterCards.Hunter;
import it.polimi.ingsw.am46.model.cards.characterCards.Inventor;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
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

import static javafx.beans.binding.Bindings.when;
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

    @Test
    void testSustenanceDiscountsCumulative() {
        player.addCard(new Artist(1, 1, 0, 2));
        player.addCard(new Artist(2, 1, 0, 2));
        player.addCard(new Inventor(3, 1, 0, Item.ARROW, 2));
        player.addCard(new Gatherer(4, 1, 0, 2));

        RoundPhase eventPhase = new MockPhase(TriggerType.ONEVENT);

        BuildingCard eff2 = BuildingFactory.createBuilding(1, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT2);
        BuildingCard eff20 = BuildingFactory.createBuilding(2, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT20);
        BuildingCard eff21 = BuildingFactory.createBuilding(3, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT21);

        ctx.setCurrentEvent(new Hunt(99, 1, 0, false, 0));
        eff2.applyEffect(eventPhase, ctx);
        assertEquals(0, player.getSustenanceDiscount(), "Non deve applicarsi se l'evento non è Sostentamento");

        ctx.setCurrentEvent(new Sustenance(100, 1, 0, false, 2));

        eff2.applyEffect(eventPhase, ctx);   // +2 sconto (Artisti)
        eff20.applyEffect(eventPhase, ctx);  // +1 sconto (Inventori)
        eff21.applyEffect(eventPhase, ctx);  // +1 sconto (Gatherers)

        assertEquals(4, player.getSustenanceDiscount(), "Total discount must be 4");
    }

    @Test
    void testDynamicFoodAndFlags() {
        RoundPhase addCardPhase = new MockPhase(TriggerType.ADDCARD);

        BuildingCard eff3 = BuildingFactory.createBuilding(1, 1, 0, 0, 0, TriggerType.ADDCARD, EffectID.EFFECT3);
        player.setNewlyFormedInventorPairs(2);
        eff3.applyEffect(addCardPhase, ctx);
        assertEquals(6, player.getFood());
        player.resetNewlyFormedInventorPairs();

        BuildingCard eff4 = BuildingFactory.createBuilding(11, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT4);
        player.addCard(new Hunter(12, 1, 0, false, 2));
        ctx.setCurrentEvent(new Hunt(101, 1, 0, false, 0)); // Setto l'evento caccia
        eff4.applyEffect(new MockPhase(TriggerType.ONEVENT), ctx);
        assertEquals(7, player.getFood()); // 6 + 1
        assertEquals(1, player.getPP());   // 0 + 1

        // Test EFFECT 8 (Pitture Rupestri)
        BuildingCard eff8 = BuildingFactory.createBuilding(13, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT8);
        player.addCard(new Artist(14, 1, 0, 2));
        ctx.setCurrentEvent(new CavePaintings(103, 1, 0, false, 1, 0, 0));
        eff8.applyEffect(new MockPhase(TriggerType.ONEVENT), ctx);
        assertEquals(8, player.getFood()); // 7 + 1

        BuildingCard eff9 = BuildingFactory.createBuilding(2, 1, 0, 0, 0, TriggerType.ADDCARD, EffectID.EFFECT9);
        player.setNewlyFormedSets(1);
        eff9.applyEffect(addCardPhase, ctx);
        assertEquals(13, player.getFood()); // 8 + 5

        RoundPhase postEventPhase = new MockPhase(TriggerType.ONEVENT);
        BuildingCard eff11 = BuildingFactory.createBuilding(3, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT11);
        eff11.applyEffect(postEventPhase, ctx);
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

        RoundPhase placementPhase = new MockPhase(TriggerType.ONTOTEMPLACEMENT);
        BuildingCard eff10 = BuildingFactory.createBuilding(4, 1, 0, 0, 0, TriggerType.ONTOTEMPLACEMENT, EffectID.EFFECT10);

        eff10.applyEffect(placementPhase, ctx);
        assertEquals(14, player.getFood()); // 13 + 1
    }

    @Test
    void testShamanFlagsCumulative() {
        RoundPhase eventPhase = new MockPhase(TriggerType.ONEVENT);

        BuildingCard eff5 = BuildingFactory.createBuilding(1, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT5);
        BuildingCard eff6 = BuildingFactory.createBuilding(2, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT6);
        BuildingCard eff7 = BuildingFactory.createBuilding(3, 1, 0, 0, 0, TriggerType.ONEVENT, EffectID.EFFECT7);

        // Impostiamo l'evento a Shamanic Ritual per soddisfare l'IF delle lambda
        ctx.setCurrentEvent(new ShamanicRitual(102, 1, 0, false, 0, 0));

        eff5.applyEffect(eventPhase, ctx);
        eff6.applyEffect(eventPhase, ctx);
        eff7.applyEffect(eventPhase, ctx);

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
