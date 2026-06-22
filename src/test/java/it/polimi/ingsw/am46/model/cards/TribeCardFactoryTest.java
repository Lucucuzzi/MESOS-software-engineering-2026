package it.polimi.ingsw.am46.model.cards;

import it.polimi.ingsw.am46.network.dto.CardDataDTO.CharacterDTO;
import it.polimi.ingsw.am46.network.dto.CardDataDTO.EventDTO;
import it.polimi.ingsw.am46.server.model.cards.TribeCardFactory;
import it.polimi.ingsw.am46.server.model.cards.characterCards.*;
import it.polimi.ingsw.am46.server.model.cards.eventCards.CavePaintings;
import it.polimi.ingsw.am46.server.model.cards.eventCards.Hunt;
import it.polimi.ingsw.am46.server.model.cards.eventCards.ShamanicRitual;
import it.polimi.ingsw.am46.server.model.cards.eventCards.Sustenance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TribeCardFactoryTest {

    private CharacterDTO createMockCharacterDTO(String subType) {
        CharacterDTO dto = new CharacterDTO();
        dto.id = 1;
        dto.era = 1;
        dto.cost = 0;
        dto.minPlayers = 2;
        dto.SubType = subType;
        return dto;
    }

    private EventDTO createMockEventDTO(String subType) {
        EventDTO dto = new EventDTO();
        dto.id = 2;
        dto.era = 1;
        dto.cost = 0;
        dto.SubType = subType;
        return dto;
    }

    @Test
    void ShouldCreateCharacterSuccessForStandardCharacters() {
        // Assert Artist
        assertTrue(TribeCardFactory.createCharacter(createMockCharacterDTO("ARTIST")) instanceof Artist);
        // Assert Builder
        assertTrue(TribeCardFactory.createCharacter(createMockCharacterDTO("BUILDER")) instanceof Builder);
        // Assert Gatherer
        assertTrue(TribeCardFactory.createCharacter(createMockCharacterDTO("GATHERER")) instanceof Gatherer);
        // Assert Hunter
        assertTrue(TribeCardFactory.createCharacter(createMockCharacterDTO("HUNTER")) instanceof Hunter);
        // Assert Shaman
        assertTrue(TribeCardFactory.createCharacter(createMockCharacterDTO("SHAMAN")) instanceof Shaman);
        // Assert Inventor
        CharacterDTO inventor = createMockCharacterDTO("INVENTOR");
        inventor.item = "ARROW";
        assertTrue(TribeCardFactory.createCharacter(inventor) instanceof Inventor);
    }

    @Test
    void ShouldThrowsExceptionWhenItemIsNull() {
        CharacterDTO dto = createMockCharacterDTO("INVENTOR");
        dto.item = null;

        assertThrows(IllegalArgumentException.class, () -> {
            TribeCardFactory.createCharacter(dto);
        }, "Should throw exception when Inventor has no item");
    }

    @Test
    void ShouldThrowsExceptionWhenItemIsInvalid() {
        CharacterDTO dto = createMockCharacterDTO("INVENTOR");
        dto.item = "NONEXISTENT_ITEM"; // Value not present in Item enum

        assertThrows(IllegalArgumentException.class, () -> {
            TribeCardFactory.createCharacter(dto);
        }, "Should throw exception when Inventor has an invalid item string");
    }

    @Test
    void ShouldThrowsExceptionForUnknownSubType() {
        CharacterDTO dto = createMockCharacterDTO("UNKNOWN_CHARACTER");

        assertThrows(IllegalArgumentException.class, () -> {
            TribeCardFactory.createCharacter(dto);
        }, "Should throw exception for unregistered character subtype");
    }

    @Test
    void ShouldCreateEventSuccessForAllEventTypes() {
        // Assert Cave Paintings
        assertTrue(TribeCardFactory.createEvent(createMockEventDTO("CAVEP")) instanceof CavePaintings);
        // Assert Hunt
        assertTrue(TribeCardFactory.createEvent(createMockEventDTO("HUNT")) instanceof Hunt);
        // Assert Shamanic Ritual
        assertTrue(TribeCardFactory.createEvent(createMockEventDTO("SHR")) instanceof ShamanicRitual);
        // Assert Sustenance
        assertTrue(TribeCardFactory.createEvent(createMockEventDTO("SUSTENANCE")) instanceof Sustenance);
    }

    @Test
    void ShouldCreateEventThrowsExceptionForUnknownSubType() {
        EventDTO dto = createMockEventDTO("UNKNOWN_EVENT");

        assertThrows(IllegalArgumentException.class, () -> {
            TribeCardFactory.createEvent(dto);
        }, "Should throw exception for unregistered event subtype");
    }
}