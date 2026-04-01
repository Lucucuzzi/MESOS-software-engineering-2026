package it.polimi.ingsw.am46.model.cards.buildingCards;



import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.eventCards.Hunt;
import it.polimi.ingsw.am46.model.cards.eventCards.Sustenance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompleteBuildingEffectTest {

    private Game game;
    private Player p1;

    @BeforeEach
    void setupGame() {
        game = new Game();
        p1 = new Player("Orazio");
        game.getPlayers().add(p1);
        game.setActivePlayer(p1);
    }

    @Test
    void testNewlyFormedInventorPairsEffect() {
        System.out.println("=== TEST: EFFECT3 (NEW INVENTOR PAIRS) ===");

        // Setup the lambda exactly as it is in BuildingFactory (EFFECT3)
        BuildingEffect effect3 = ctx -> {
            Player p = ctx.getActivePlayer();
            int newPairs = p.getNewlyFormedInventorPairs();
            if (newPairs > 0) {
                p.modifyFood(newPairs * 3);
            }
        };

        p1.modifyFood(10); // Starting food
        assertEquals(10, p1.getFood());

        // Add the first Inventor (BOAT)
        Inventor inv1 = new Inventor(101, 1, 0, Item.BOAT, 2);
        simulateAddCardProcess(game, p1, inv1, effect3);

        // Pair is not complete yet. Food must remain 10.
        assertEquals(10, p1.getFood(), "No complete pair yet, food should not increase!");

        // Add the second Inventor (BOAT) to complete the pair!
        Inventor inv2 = new Inventor(102, 1, 0, Item.BOAT, 2);
        simulateAddCardProcess(game, p1, inv2, effect3);

        // Now the pair is complete, newlyFormedInventorPairs was 1. Food must be 13 (+3).
        assertEquals(13, p1.getFood(), "Pair completed! The player MUST receive 3 Food.");

        // SECURE TEST: Trigger the effect AGAIN without adding new pairs.
        // This ensures the lambda strictly relies on getNewlyFormedInventorPairs()
        p1.resetNewlyFormedInventorPairs(); // Game resets flags after the phase
        effect3.apply(game);

        assertEquals(13, p1.getFood(), "Triggering the effect again without a NEW pair MUST NOT give extra food!");
        System.out.println("Correct: Only strictly NEW inventor pairs grant the bonus.");
    }

    @Test
    void testNewlyFormedCompleteSetsEffect() {
        System.out.println(" TEST: EFFECT9 (NEW COMPLETE SETS) ");

        // Setup the lambda exactly as it is in BuildingFactory (EFFECT9)
        BuildingEffect effect9 = ctx -> {
            Player p = ctx.getActivePlayer();
            int newSets = p.getNewlyFormedSets();
            if (newSets > 0) {
                p.modifyFood(newSets * 5);
            }
        };

        p1.modifyFood(5); // Starting food

        // Add 5 out of 6 required character types
        p1.addCard(new Hunter(201, 1, 0, false, 2));
        p1.addCard(new Shaman(202, 1, 0, 1, 2));
        p1.addCard(new Artist(203, 1, 0, 2));
        p1.addCard(new Builder(204, 1, 0, 2, 1, 2));
        p1.addCard(new Gatherer(205, 1, 0, 2));

        // The set is missing an Inventor. newlyFormedSets = 0.
        simulateAddCardProcess(game, p1, new Hunter(206, 1, 0, false, 2), effect9); // Add a duplicate hunter
        assertEquals(5, p1.getFood(), "Set is not complete yet, food should remain the same.");

        // Now add the missing Inventor to complete the set!
        Inventor missingInventor = new Inventor(207, 1, 0, Item.ARROW, 2);
        simulateAddCardProcess(game, p1, missingInventor, effect9);

        // Set is complete! Player should get +5 Food.
        assertEquals(10, p1.getFood(), "Set completed! The player MUST receive 5 Food.");

        // Try applying the effect again directly
        p1.resetNewlyFormedSets();
        effect9.apply(game);
        assertEquals(10, p1.getFood(), "Triggering the effect again without a NEW set MUST NOT give extra food!");
        System.out.println("Correct: Only strictly NEW character sets grant the 5 Food bonus.");
    }

    @Test
    void testEventBuildingEffects() {
        System.out.println(" TEST: EVENT MODIFIERS (SUSTENANCE and HUNT) ");

        // EFFECT2: Sustenance discount (1 per Artist)
        BuildingEffect effect2 = ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType() != SubType.SUSTENANCE) return;
            Player player = ctx.getActivePlayer();
            int artists = player.countCharactersByType(SubType.ARTIST);
            if (artists > 0) {
                player.addSustenanceDiscount(artists);
            }
        };

        // EFFECT4: Hunt bonus (+1 Food, +1 PP per Hunter)
        BuildingEffect effect4 = ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType() != SubType.HUNT) return;
            Player p = ctx.getActivePlayer();
            int hunters = p.countCharactersByType(SubType.HUNTER);
            p.modifyFood(hunters);
            p.modifyPP(hunters);
        };

        // Give player 2 Artists and 3 Hunters
        p1.addCard(new Artist(301, 1, 0, 2));
        p1.addCard(new Artist(302, 1, 0, 2));
        p1.addCard(new Hunter(303, 1, 0, false, 2));
        p1.addCard(new Hunter(304, 1, 0, false, 2));
        p1.addCard(new Hunter(305, 1, 0, false, 2));

        // 1. Simulate SUSTENANCE Event
        game.setCurrentEvent(new Sustenance(901, 1, 0, false, 2));
        effect2.apply(game); // Apply building effect

        // Player has 2 Artists, so sustenance discount should be 2
        assertEquals(2, p1.getSustenanceDiscount(), "Player should have received a sustenance discount of 2 (1 for each Artist)!");

        // 2. Simulate HUNT Event
        game.setCurrentEvent(new Hunt(902, 1, 0, false, 2));
        p1.modifyFood(0);
        p1.modifyPP(0);
        effect4.apply(game); // Apply building effect

        // Player has 3 Hunters, so should receive +3 Food and +3 PP from the BUILDING
        assertEquals(3, p1.getFood(), "Player should have received +3 Food from the Hunt Building!");
        assertEquals(3, p1.getPP(), "Player should have received +3 PP from the Hunt Building!");

        System.out.println("Correct");
    }

    @Test
    void testEndGameBuildingEffects() {
        System.out.println("=== TEST: END GAME EFFECTS (PP BONUSES) ===");

        // EFFECT13: 6 PP per complete set at end game
        BuildingEffect effect13 = ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCompleteSets() * 6);
        };

        // Add 1 complete set to the player
        p1.addCard(new Hunter(401, 1, 0, false, 2));
        p1.addCard(new Shaman(402, 1, 0, 1, 2));
        p1.addCard(new Artist(403, 1, 0, 2));
        p1.addCard(new Builder(404, 1, 0, 2, 1, 2));
        p1.addCard(new Gatherer(405, 1, 0, 2));
        p1.addCard(new Inventor(406, 1, 0, Item.HOOK, 2));

        assertEquals(1, p1.countCompleteSets(), "Player should have exactly 1 complete set!");

        p1.modifyPP(10); // Start with 10 PP
        effect13.apply(game);

        assertEquals(16, p1.getPP(), "Player should have gained 6 PP from the complete set at the end of the game!");
        System.out.println("Correct");
    }

    /**
     * Helper method that perfectly simulates the state machine's process of calculating
     * diffs for pairs and sets before and after adding a card.
     */
    private void simulateAddCardProcess(GameContext ctx, Player player, CharacterCard cardToAdd, BuildingEffect effectLambda) {

        int olderInventorPairs = player.countInventorPairs();
        int olderCompleteSets = player.countCompleteSets();


        player.addCard(cardToAdd);


        int currentInventorPairs = player.countInventorPairs();
        int currentCompleteSets = player.countCompleteSets();


        player.setNewlyFormedInventorPairs(Math.max(0, currentInventorPairs - olderInventorPairs));
        player.setNewlyFormedSets(Math.max(0, currentCompleteSets - olderCompleteSets));


        effectLambda.apply(ctx);
    }
}