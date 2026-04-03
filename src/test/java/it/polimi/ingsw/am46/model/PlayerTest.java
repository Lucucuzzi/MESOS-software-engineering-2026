package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Riccardo");
    }

    @Test
    void testBasicPropertiesAndModifiers() {
        //  Verify initial state (Constructor)
        assertEquals("Riccardo", player.getNickname());
        assertNull(player.getColor());
        assertEquals(0, player.getFood());
        assertEquals(0, player.getPP());
        assertTrue(player.getBuildings().isEmpty());
        assertTrue(player.getCharacters().isEmpty());

        // Base modifiers and setters
        player.setColor(Color.RED);
        assertEquals(Color.RED, player.getColor());

        player.modifyFood(5);
        player.modifyFood(-2);
        assertEquals(3, player.getFood(), "Final food should be 3 (5 - 2)");

        player.modifyPP(10);
        player.modifyPP(-3);
        assertEquals(7, player.getPP(), "Final PP should be 7 (10 - 3)");
    }

    @Test
    void testFlagsLifecycle() {
        //  Verify that all flags start disabled/zero
        assertFalse(player.hasShamanImmunity());
        assertFalse(player.hasShamanDoublePP());
        assertFalse(player.canTakeExtraCard());
        assertEquals(0, player.getSustenanceDiscount());
        assertEquals(0, player.getExtraShamanIcons());

        //  Simulate building effects turning on all flags
        player.setShamanImmunity(true);
        player.setShamanDoublePP(true);
        player.setCanTakeExtraCard(true);
        player.addSustenanceDiscount(2);
        player.addSustenanceDiscount(1); // Testing cumulative sum
        player.addExtraShamanIcons(); // Adds 3
        player.addExtraShamanIcons(); // Adds another 3

        //  Verify correct activation and accumulation
        assertTrue(player.hasShamanImmunity());
        assertTrue(player.hasShamanDoublePP());
        assertTrue(player.canTakeExtraCard());
        assertEquals(3, player.getSustenanceDiscount());
        assertEquals(6, player.getExtraShamanIcons());

        //  Test the reset (Simulating the end of an event/round)
        player.resetShamanFlags();
        player.resetSustenanceDiscount();
        player.resetExtraCard();

        // Verify everything is back to 0 or false
        assertFalse(player.hasShamanImmunity());
        assertFalse(player.hasShamanDoublePP());
        assertFalse(player.canTakeExtraCard());
        assertEquals(0, player.getSustenanceDiscount());
        assertEquals(0, player.getExtraShamanIcons());
    }

    @Test
    void testCardManagementAndComplexCounting() {
        // Add a Building to cover the getBuildings() method
        BuildingCard bCard = new BuildingCard(1, 1, 0, 0, TriggerType.ONEVENT, ctx -> {});
        player.addCard(bCard);
        assertEquals(1, player.getBuildings().size());

        // Create a complete Tribe (1 card per type, plus an extra hunter)
        player.addCard(new Hunter(2, 1, 0, false, 2));
        player.addCard(new Hunter(3, 1, 0, true, 2));
        player.addCard(new Shaman(4, 1, 0, 2, 2)); // Shaman with 2 stars
        player.addCard(new Artist(5, 1, 0, 2));
        player.addCard(new Builder(6, 1, 0, 2, 1, 2));
        player.addCard(new Inventor(7, 1, 0, Item.ARROW, 2));
        player.addCard(new Gatherer(8, 1, 0, 2));

        assertEquals(7, player.getCharacters().size());

        // Coverage for countCharactersByType (Testing one present and one absent type)
        assertEquals(2, player.countCharactersByType(SubType.HUNTER));
        assertEquals(0, player.countCharactersByType(SubType.CAVEP));

        // Coverage for countCompleteSets (Having 1 of everything guarantees 1 complete set)
        assertEquals(1, player.countCompleteSets(), "Must find exactly 1 complete set");

        // Coverage for countShamanIcons (Adding the building bonus to test the final sum)
        player.addExtraShamanIcons();
        assertEquals(5, player.countShamanIcons(), "2 card stars + 3 extra = 5");


        assertEquals(0, player.countInventorPairs(), "0 pairs of inventors");

        assertEquals(2, player.calculateBuilderPP(), "2pp from builders");
    }
}