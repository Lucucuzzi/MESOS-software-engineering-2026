package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardTest {

    @Test
    void testArtistAndBaseCardMethods() {
        // Arrange: Use 0 to test the ternary operator (minPlayers == 0 ? 2 : minPlayers)
        Artist artist = new Artist(1, 1, 5, 0);

        // Assert: Base Card & TribeCard getters
        assertEquals(1, artist.getId());
        assertEquals(1, artist.getEra());
        assertEquals(5, artist.getCost());
        assertEquals(Type.CHARACTER, artist.getType());
        assertEquals(SubType.ARTIST, artist.getSubType());
        assertEquals(2, artist.getMinPlayers());

        // Assert: Base empty methods in Card.java to hit the lines
        Player dummyPlayer = new Player("Dummy");
        assertDoesNotThrow(() -> artist.applyEffect(dummyPlayer));
        assertDoesNotThrow(() -> artist.resolve(null));
    }

    @Test
    void testBuilderProperties() {
        Builder builder = new Builder(2, 2, 0, 10, 3, 4);
        assertEquals(SubType.BUILDER, builder.getSubType());
        assertEquals(10, builder.getPp());
        assertEquals(3, builder.getDiscount());
        assertEquals(4, builder.getMinPlayers());
    }

    @Test
    void testGathererProperties() {
        Gatherer gatherer = new Gatherer(3, 1, 0, 5);
        assertEquals(SubType.GATHERER, gatherer.getSubType());
        assertEquals(5, gatherer.getMinPlayers());
    }

    @Test
    void testInventorProperties() {
        Inventor inventor = new Inventor(4, 3, 0, Item.FLUTE, 0);
        assertEquals(SubType.INVENTOR, inventor.getSubType());
        assertEquals(Item.FLUTE, inventor.getItem());
        assertEquals(2, inventor.getMinPlayers());
    }

    @Test
    void testShamanProperties() {
        Shaman shaman = new Shaman(5, 2, 0, 3, 3);
        assertEquals(SubType.SHAMAN, shaman.getSubType());
        assertEquals(3, shaman.getStars());
        assertEquals(3, shaman.getMinPlayers());
    }

    @Test
    void testHunterPropertiesAndEffect() {
        Hunter normalHunter = new Hunter(6, 1, 0, false, 0);
        Hunter foodHunter = new Hunter(7, 1, 0, true, 0);
        Player player = new Player("HunterTest");

        // Assert basic properties
        assertEquals(SubType.HUNTER, normalHunter.getSubType());
        assertFalse(normalHunter.isFood());
        assertTrue(foodHunter.isFood());

        // Arrange: Add hunters to player to test effect
        player.addCard(normalHunter);
        player.addCard(foodHunter);

        // Act & Assert: Normal hunter does nothing
        normalHunter.applyEffect(player);
        assertEquals(0, player.getFood());

        // Act & Assert: Food hunter adds food based on total hunters (2)
        foodHunter.applyEffect(player);
        assertEquals(2, player.getFood());
    }
}