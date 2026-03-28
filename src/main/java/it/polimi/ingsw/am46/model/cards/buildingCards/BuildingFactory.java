package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.Space;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.TurnTile;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

import java.util.HashMap;
import java.util.Map;

public class BuildingFactory {
    private static final Map<EffectID, BuildingEffect> effectRegistry = new HashMap<>();


    static {
        // Flat 25 PP in endGame
        effectRegistry.put(EffectID.EFFECT1, ctx -> {
            System.out.println("Executing 25 PP effect...");
            ctx.getActivePlayer().modifyPP(25);
        });

        // Sustenance discount for artists
        effectRegistry.put(EffectID.EFFECT2, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            System.out.println("Applying sustain discount for Artists");
            Player player = ctx.getActivePlayer();
            int artists = player.countCharactersByType(SubType.ARTIST);
            if (artists > 0) {
                int currentDiscount = player.getSustenanceDiscount();
                player.addSustenanceDiscount(artists);
            }
        });

        // Inventor pairs bonus
        effectRegistry.put(EffectID.EFFECT3, ctx -> {
            System.out.println("Checking inventor pairs for 3 Food...");
            // Logica di controllo sull'inventario del giocatore va nella fase addcard dello state
            //controlla se countinventorpairs prima e dopo l'aggiunta della carta aumenta
            //se aumenta, mette di quanto è aumentato in getNewlyFormedInventorPairs
            Player p = ctx.getActivePlayer();
            int newPairs = p.getNewlyFormedInventorPairs();
            if (newPairs > 0) {
                p.modifyFood(newPairs * 3);
            }

        });
        effectRegistry.put(EffectID.EFFECT4, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.HUNT) return;
            System.out.println("Applying hunt bonus: +1 food and +1 PP per hunter...");
            Player p = ctx.getActivePlayer();
            int hunters = p.countCharactersByType(SubType.HUNTER);
            p.modifyFood(hunters);
            p.modifyPP(hunters);
        });

        // Shamanic immunity
        effectRegistry.put(EffectID.EFFECT5, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            System.out.println("Applying shaman immunity to PP loss...");
            ctx.getActivePlayer().setShamanImmunity(true);
        });

        // ExtraShamanic icons
        effectRegistry.put(EffectID.EFFECT6, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            System.out.println("Adding 3 extra shaman icons...");
            ctx.getActivePlayer().addExtraShamanIcons();
        });

        // double shamanPP
        effectRegistry.put(EffectID.EFFECT7, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            System.out.println("Enabling double PP if shaman icons majority...");
            ctx.getActivePlayer().setShamanDoublePP(true);
        });

        // CavePaintings, 1 extra food per artists
        effectRegistry.put(EffectID.EFFECT8, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.CAVEP) return;
            System.out.println("Applying cave art food bonus per artist...");
            Player p = ctx.getActivePlayer();
            p.modifyFood(p.countCharactersByType(SubType.ARTIST));
        });

        // everytime you get a new set, add 5 food
        effectRegistry.put(EffectID.EFFECT9, ctx -> {
            Player p = ctx.getActivePlayer();
            int newSets = p.getNewlyFormedSets();
            if (newSets > 0) {
                p.modifyFood(newSets * 5);
            }
        });

        // 1 extra food if space bonus
        effectRegistry.put(EffectID.EFFECT10, ctx -> {
            Player p = ctx.getActivePlayer();
            TurnTile turnTile = ctx.getBoard().getTurnTile();
            if (turnTile != null) {
                Space playerSpace = turnTile.getSpaceOfPlayer(p);
                if (playerSpace != null && playerSpace.getFood() > 0) {
                    p.modifyFood(1);
                }
            }
        });

        // extra draw
        effectRegistry.put(EffectID.EFFECT11, ctx -> {
            System.out.println("Enabling extra card before end round...");
            ctx.getActivePlayer().setCanTakeExtraCard(true);
        });

        // double PP for builders at endgame
        effectRegistry.put(EffectID.EFFECT12, ctx -> {
            System.out.println("Applying double builder PP at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.calculateBuilderPP()); // not 2* because BuilderPP are added default at endgame (so with this building they became 2*)
        });

        // 6 PP for every complete set
        effectRegistry.put(EffectID.EFFECT13, ctx -> {
            System.out.println("Applying 6 PP per complete set at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCompleteSets() * 6);
        });

        // 3 PP for each hunter in endgame
        effectRegistry.put(EffectID.EFFECT14, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.HUNTER) * 3);
        });

        // 4 pp for each gatherer in endgame
        effectRegistry.put(EffectID.EFFECT15, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.GATHERER) * 4);
        });

        // 4pp for each shaman in endgame
        effectRegistry.put(EffectID.EFFECT16, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.SHAMAN) * 4);
        });

        // 4 pp for each builder in endgame
        effectRegistry.put(EffectID.EFFECT17, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.BUILDER) * 4);
        });

        // 4 pp for each artist in endgame
        effectRegistry.put(EffectID.EFFECT18, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.ARTIST) * 4);
        });

        // 2 pp for each inventor in endgame
        effectRegistry.put(EffectID.EFFECT19, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.INVENTOR) * 2);
        });

        // Sustenance discount for inventor
        effectRegistry.put(EffectID.EFFECT20, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            System.out.println("Applying sustain discount for Inventor");
            Player player = ctx.getActivePlayer();
            int inventors = player.countCharactersByType(SubType.INVENTOR);
            if (inventors > 0) {
                int currentDiscount = player.getSustenanceDiscount();
                player.addSustenanceDiscount(inventors);
            }
        });

        // Sustenance discount for gatherer
        effectRegistry.put(EffectID.EFFECT21, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            System.out.println("Applying sustain discount for Gatherer");
            Player player = ctx.getActivePlayer();
            int gatherers = player.countCharactersByType(SubType.GATHERER);
            if (gatherers > 0) {
                int currentDiscount = player.getSustenanceDiscount();
                player.addSustenanceDiscount(gatherers);
            }
        });




    }

    public static BuildingCard createBuilding(int id, int era, int cost, int pp, int food, TriggerType triggerType, EffectID effectId) {
        //I get the correct effect from the register
        BuildingEffect effect = effectRegistry.get(effectId);
        // I create the new card with the strategy implemented
        return new BuildingCard(id, era, cost, pp, food, triggerType, effect);
    }

}
