package it.polimi.ingsw.am46.model.cards.characterCards;


import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardTest {

    @Test
    void testArtistAndBaseCardMethods() {
        Artist artist = new Artist(1, 1, 5, 0);

        assertEquals(1, artist.getId());
        assertEquals(1, artist.getEra());
        assertEquals(5, artist.getCost());
        assertEquals(Type.CHARACTER, artist.getType());
        assertEquals(SubType.ARTIST, artist.getSubType());
        assertEquals(2, artist.getMinPlayers());

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

        assertEquals(Optional.of(Item.FLUTE), inventor.getItem());
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

        player.addCard(normalHunter);
        assertEquals(0, player.getFood(), "hunter without food doesn't give you extra food");


        player.addCard(foodHunter);
        assertEquals(2, player.getFood(), "Hunter with food gives you food bonus");
    }
}