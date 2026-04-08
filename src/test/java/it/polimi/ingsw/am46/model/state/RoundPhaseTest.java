package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundPhaseTest {

    // A minimal concrete class to test the abstract methods of RoundPhase
    private static class DummyPhase extends RoundPhase {
        public DummyPhase() {
            super(TriggerType.ADDCARD);
        }
    }

    private RoundPhase phase;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        phase = new DummyPhase();
        game = new Game();
        player = new Player("Alice");
    }

    @Test
    void testDefaultExceptions() {
        // Assert that calling non-overriden handlers correctly throws the Exception
        assertThrows(IllegalStateException.class, () -> phase.handlePlaceTotem(game, player, null));
        assertThrows(IllegalStateException.class, () -> phase.handleAddCard(game, player, null));
        assertThrows(IllegalStateException.class, () -> phase.handleResolveEvent(game));
        assertThrows(IllegalStateException.class, () -> phase.handleDrawExtraCard(game, player, null));
        assertThrows(IllegalStateException.class, () -> phase.handleEndRound(game));
    }

    @Test
    void testGettersAndBooleans() {
        assertEquals(TriggerType.ADDCARD, phase.getTriggerType());
        assertFalse(phase.isFinalPhase(), "Default RoundPhase should not be marked as final");
    }



}