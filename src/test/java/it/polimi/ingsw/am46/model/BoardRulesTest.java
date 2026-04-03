package it.polimi.ingsw.am46.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class BoardRulesTest {

    @Test
    void ShouldCreateTurnTileSpacesForValidPlayers() {
        List<Space> spaces2 = BoardRules.createTurnTileSpaces(2);
        assertEquals(2, spaces2.size());

        List<Space> spaces3 = BoardRules.createTurnTileSpaces(3);
        assertEquals(3, spaces3.size());

        List<Space> spaces4 = BoardRules.createTurnTileSpaces(4);
        assertEquals(4, spaces4.size());

        List<Space> spaces5 = BoardRules.createTurnTileSpaces(5);
        assertEquals(5, spaces5.size());

        // in case of the number of player is invalid, BoardRules should throw an exception
        assertThrows(IllegalArgumentException.class, () -> BoardRules.createTurnTileSpaces(1));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.createTurnTileSpaces(6));
    }

    @Test
    void ShouldCreateOfferTilesForValidPlayers() {

        List<OfferTile> tiles2 = BoardRules.createOfferTiles(2);
        assertEquals(4, tiles2.size());

        List<OfferTile> tiles3 = BoardRules.createOfferTiles(3);
        assertEquals(5, tiles3.size());

        List<OfferTile> tiles4 = BoardRules.createOfferTiles(4);
        assertEquals(6, tiles4.size());

        List<OfferTile> tiles5 = BoardRules.createOfferTiles(5);
        assertEquals(7, tiles5.size());

        // in case of the number of player is invalid, BoardRules should throw an exception
        assertThrows(IllegalArgumentException.class, () -> BoardRules.createOfferTiles(1));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.createOfferTiles(8));
    }


    @Test
    void TestGetterBuildingEra1() {
        assertEquals(1, BoardRules.getBuildingsPerEra(2, 1));
        assertEquals(2, BoardRules.getBuildingsPerEra(3, 1));
        assertEquals(2, BoardRules.getBuildingsPerEra(4, 1));
        assertEquals(2, BoardRules.getBuildingsPerEra(5, 1));

        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(1, 1));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(6, 1));
    }

    @Test
    void TestGetterBuildingEra2() {
        assertEquals(2, BoardRules.getBuildingsPerEra(2, 2));
        assertEquals(2, BoardRules.getBuildingsPerEra(3, 2));
        assertEquals(3, BoardRules.getBuildingsPerEra(4, 2));
        assertEquals(3, BoardRules.getBuildingsPerEra(5, 2));

        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(1, 2));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(6, 2));
    }

    @Test
    void TestGetterBuildingEra3() {
        assertEquals(3, BoardRules.getBuildingsPerEra(2, 3));
        assertEquals(4, BoardRules.getBuildingsPerEra(3, 3));
        assertEquals(4, BoardRules.getBuildingsPerEra(4, 3));
        assertEquals(5, BoardRules.getBuildingsPerEra(5, 3));

        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(1, 3));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(6, 3));
    }

    @Test
    void TestGetterBuildingEraInvalidEra() {
        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(3, 0));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.getBuildingsPerEra(3, 4));
    }
}