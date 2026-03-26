package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TestGameContext;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.state.RoundPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingEndGameCardTest {
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
    void testEndGamePrestigePointsCalculation() {
        player.modifyPP(10); // Start with 10 PP

        // Creiamo una tribù con 1 set completo (1 carta per tipo)
        player.addCard(new Hunter(1, 1, 0, false, 2));
        player.addCard(new Builder(2, 1, 0, 5, 1, 2)); // 5 PP stampati
        player.addCard(new Shaman(3, 1, 0, 2, 2));
        player.addCard(new Artist(4, 1, 0, 2));
        player.addCard(new Gatherer(5, 1, 0, 2));
        player.addCard(new Inventor(6, 1, 0, Item.ARROW, 2));

        RoundPhase endPhase = new MockPhase(TriggerType.ENDTURN);

        // EFFECT 1: 25 Punti Prestigio fissi
        BuildingCard eff1 = BuildingFactory.createBuilding(101, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT1);
        eff1.applyEffect(endPhase, ctx);
        assertEquals(35, player.getPP()); // 10 + 25

        // EFFECT 12: Doppio dei PP indicati sulle carte Costruttore
        BuildingCard eff12 = BuildingFactory.createBuilding(102, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT12);
        eff12.applyEffect(endPhase, ctx);
        assertEquals(40, player.getPP()); // 35 + 5

        // EFFECT 13: 6 PP per ogni set di 6 carte
        BuildingCard eff13 = BuildingFactory.createBuilding(103, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT13);
        eff13.applyEffect(endPhase, ctx);
        assertEquals(46, player.getPP()); // 40 + 6

        // EFFECT 14-19: PP per ogni carta Personaggio specifica
        BuildingCard eff14 = BuildingFactory.createBuilding(104, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT14);
        eff14.applyEffect(endPhase, ctx);
        assertEquals(49, player.getPP()); // 46 + (1 Hunter * 3)

        BuildingCard eff15 = BuildingFactory.createBuilding(105, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT15);
        eff15.applyEffect(endPhase, ctx);
        assertEquals(53, player.getPP()); // 49 + (1 Gatherer * 4)

        BuildingCard eff16 = BuildingFactory.createBuilding(106, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT16);
        eff16.applyEffect(endPhase, ctx);
        assertEquals(57, player.getPP()); // 53 + (1 Shaman * 4)

        BuildingCard eff17 = BuildingFactory.createBuilding(107, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT17);
        eff17.applyEffect(endPhase, ctx);
        assertEquals(61, player.getPP()); // 57 + (1 Builder * 4)

        BuildingCard eff18 = BuildingFactory.createBuilding(108, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT18);
        eff18.applyEffect(endPhase, ctx);
        assertEquals(65, player.getPP()); // 61 + (1 Artist * 4)

        BuildingCard eff19 = BuildingFactory.createBuilding(109, 3, 0, 0, 0, TriggerType.ENDTURN, EffectID.EFFECT19);
        eff19.applyEffect(endPhase, ctx);
        assertEquals(67, player.getPP()); // 65 + (1 Inventor * 2)
    }

    private static class MockPhase extends RoundPhase {
        private final TriggerType mockTrigger;
        MockPhase(TriggerType triggerType) {
            this.mockTrigger = triggerType;
        }

        @Override
        public TriggerType getTriggerType() {
            return mockTrigger;
        }
    }
    }

