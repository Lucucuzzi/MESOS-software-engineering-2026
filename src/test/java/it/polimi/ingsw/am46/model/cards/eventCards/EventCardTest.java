package it.polimi.ingsw.am46.model.cards.eventCards;



import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TestGameContext;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    private Player p1, p2, p3;
    private TestGameContext context;

    @BeforeEach
    void setUp() {
        p1 = new Player("Manu");
        p2 = new Player("Orazio");
        p3 = new Player("Luca");
        context = new TestGameContext(Arrays.asList(p1, p2, p3));
    }

    @Test
    void testSustenance_WithGathererDiscountAndPenalties() {
        // P1: 4 mouths to feed. 0 Food. Has 1 Gatherer.
        // Rule: Pay 1 Food per Character. Gatherer provides 3 discounts.
        // Total cost = 4 - 3 = 1 Food.
        // Since P1 has 0 Food, 1 character remains unfed. Loses indicated PP.
        p1.addCard(new Artist(1, 1, 0, 2));
        p1.addCard(new Artist(2, 1, 0, 2));
        p1.addCard(new Artist(3, 1, 0, 2));
        p1.addCard(new Gatherer(4, 1, 0, 2));

        Sustenance event = new Sustenance(100, 1, 0, false, 2); // Penalty: 2 PP
        event.resolve(context);

        assertEquals(-2, p1.getPP(), "1 unfed character * 2 PP penalty = -2 PP");
        assertEquals(0, p1.getFood(), "Started with no food, remains at 0");

        // Getter coverage
        assertEquals(SubType.SUSTENANCE, event.getSubType());
        assertEquals(Type.EVENT, event.getType());
        assertFalse(event.isFinalEvent());
    }

    @Test
    void testHunt_Rewards() {

        p1.addCard(new Hunter(1, 1, 0, false, 2));
        p1.addCard(new Hunter(2, 1, 0, true, 2));

        assertEquals(2, p1.getFood(), "hunter with food gives you 2 food");

        Hunt event = new Hunt(101, 1, 0, false, 3); // Event gives 3 PP per hunter
        event.resolve(context);

        assertEquals(4, p1.getFood(), "2 Hunters = 2 Food + 2(initial)");
        assertEquals(6, p1.getPP(), "2 Hunters * 3 PP = 6 PP");

        // Getter coverage
        assertEquals(SubType.HUNT, event.getSubType());
    }

    @Test
    void testCavePaintings_RewardAndPenalty() {
        // Rule: Gain or lose PP based on the minimum required artists.
        p1.addCard(new Artist(1, 1, 0, 2)); // P1: 1 Artist (Below minimum)
        p2.addCard(new Artist(2, 1, 0, 2));
        p2.addCard(new Artist(3, 1, 0, 2)); // P2: 2 Artists (Satisfies minimum)

        // Event: Minimum 2 Artists. Penalty 3 PP. Reward: 2 PP per artist.
        CavePaintings event = new CavePaintings(102, 1, 0, false, 2, 3, 2);
        event.resolve(context);

        assertEquals(-3, p1.getPP(), "P1 has 1 Artist (min 2). Loses 3 PP.");
        assertEquals(4, p2.getPP(), "P2 has 2 Artists. Gains 2 * 2 = 4 PP.");

        // Getter coverage
        assertEquals(SubType.CAVEP, event.getSubType());
    }

    @Test
    void testShamanicRitual_MajorityTieAndImmunity() {
        // Rule: Most icons gain PP. Fewest icons lose PP.
        // In case of a tie, applies to all tied players.

        p1.addCard(new Shaman(1, 1, 0, 3, 2)); // P1: 3 icons
        p2.addCard(new Shaman(2, 1, 0, 3, 2)); // P2: 3 icons (Top tie)

        p3.addCard(new Shaman(3, 1, 0, 1, 2)); // P3: 1 icon (Minority)
        p3.setShamanImmunity(true); // Simulate building effect: Immunity

        // Event: +5 PP for majority, -3 PP for minority
        ShamanicRitual event = new ShamanicRitual(103, 1, 0, false, 5, 3);
        event.resolve(context);

        assertEquals(5, p1.getPP(), "P1 tied for majority. Gets 5 PP.");
        assertEquals(5, p2.getPP(), "P2 tied for majority. Gets 5 PP.");
        assertEquals(0, p3.getPP(), "P3 is in minority but has immunity active. Does not lose 3 PP.");

        assertFalse(p3.hasShamanImmunity(), "Event must reset immunity flags after resolution.");

        // Getter coverage
        assertEquals(SubType.SHR, event.getSubType());
    }

    @Test
    void testShamanicRitual_SoleWinnerDoublePP_And_Penalty() {
        // P1: Unico vincitore con flag Doppio PP attivo (3 icone)
        p1.addCard(new Shaman(1, 1, 0, 3, 2));
        p1.setShamanDoublePP(true);

        // P2 e P3 non ricevono carte Sciamano.
        // Entrambi hanno 0 icone, pareggiando per la minoranza assoluta (minIcons = 0).

        ShamanicRitual event = new ShamanicRitual(104, 1, 0, false, 5, 3); // +5 Vittoria, -3 Sconfitta
        event.resolve(context);

        assertEquals(10, p1.getPP(), "Sole winner with Double PP: 5 * 2 = 10");

        assertEquals(-3, p2.getPP(), "P2 has 0 icons (minimum) and takes the -3 penalty");
        assertEquals(-3, p3.getPP(), "P3 has 0 icons (minimum) and takes the -3 penalty");
    }
}