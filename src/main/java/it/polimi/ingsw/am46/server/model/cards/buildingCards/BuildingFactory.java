package it.polimi.ingsw.am46.server.model.cards.buildingCards;

import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.Space;
import it.polimi.ingsw.am46.server.model.TriggerType;
import it.polimi.ingsw.am46.server.model.TurnTile;
import it.polimi.ingsw.am46.network.dto.CardDataDTO;
import it.polimi.ingsw.am46.server.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Building factory.
 */
public class BuildingFactory {
    private static final Map<EffectID, BuildingEffect> effectRegistry = new HashMap<>();

    static {
        effectRegistry.put(EffectID.EFFECT1, ctx -> {
            ctx.getActivePlayer().modifyPP(25);
        });

        effectRegistry.put(EffectID.EFFECT2, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            Player player = ctx.getActivePlayer();
            int artists = player.countCharactersByType(SubType.ARTIST);
            if (artists > 0) {
                player.addSustenanceDiscount(artists);
            }
        });


        effectRegistry.put(EffectID.EFFECT3, ctx -> {
            // Player inventory control logic belongs to the AddCard state phase
            // Checks if countInventorPairs increases before and after adding the card
            // If it increases, sets the increment value in getNewlyFormedInventorPairs
            Player p = ctx.getActivePlayer();
            int newPairs = p.getNewlyFormedInventorPairs();
            if (newPairs > 0) {
                p.modifyFood(newPairs * 3);
            }

        });
        effectRegistry.put(EffectID.EFFECT4, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.HUNT) return;
            Player p = ctx.getActivePlayer();
            int hunters = p.countCharactersByType(SubType.HUNTER);
            p.modifyFood(hunters);
            p.modifyPP(hunters);
        });

        effectRegistry.put(EffectID.EFFECT5, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            ctx.getActivePlayer().setShamanImmunity(true);
        });

        // SHR → +3 icone
        effectRegistry.put(EffectID.EFFECT6, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            ctx.getActivePlayer().addExtraShamanIcons();
        });

        // SHR → double PP if you are the most icon possessor
        effectRegistry.put(EffectID.EFFECT7, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SHR) return;
            ctx.getActivePlayer().setShamanDoublePP(true);
        });

        // Cave Painting → 1 Food per Artist
        effectRegistry.put(EffectID.EFFECT8, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.CAVEP) return;
            Player p = ctx.getActivePlayer();
            p.modifyFood(p.countCharactersByType(SubType.ARTIST));
        });

        // Each time a new set is completed, add 5 food
        // Behaves very similarly to the one counting inventor pairs
        effectRegistry.put(EffectID.EFFECT9, ctx -> {
            Player p = ctx.getActivePlayer();
            int newSets = p.getNewlyFormedSets();
            if (newSets > 0) {
                p.modifyFood(newSets * 5);
            }
        });

        // Totem on bonus space → 1 food extra (ONTOTEMREPLACEMENT)
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

        // End round possibilty to draw an extra card
        effectRegistry.put(EffectID.EFFECT11, ctx -> {
            ctx.getActivePlayer().setCanTakeExtraCard(true);
        });

        // Double PP builder in endgame
        effectRegistry.put(EffectID.EFFECT12, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.calculateBuilderPP());
        });

        // 6 PP per ogni set completo a fine partita
        effectRegistry.put(EffectID.EFFECT13, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCompleteSets() * 6);
        });

        // 3 PP for each hunter in endgame
        effectRegistry.put(EffectID.EFFECT14, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.HUNTER) * 3);
        });

        // 4 pp for each gatherer in endgame
        effectRegistry.put(EffectID.EFFECT15, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.GATHERER) * 4);
        });

        // 4pp for each shaman in endgame
        effectRegistry.put(EffectID.EFFECT16, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.SHAMAN) * 4);
        });

        // 4 pp for each builder in endgame
        effectRegistry.put(EffectID.EFFECT17, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.BUILDER) * 4);
        });

        // 4 pp for each artist in endgame
        effectRegistry.put(EffectID.EFFECT18, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.ARTIST) * 4);
        });

        // 2 pp for each inventor in endgame
        effectRegistry.put(EffectID.EFFECT19, ctx -> {
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCharactersByType(SubType.INVENTOR) * 2);
        });

        effectRegistry.put(EffectID.EFFECT20, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            Player player = ctx.getActivePlayer();
            int inventors = player.countCharactersByType(SubType.INVENTOR);
            if (inventors > 0) {
                player.addSustenanceDiscount(inventors);
            }
        });

        effectRegistry.put(EffectID.EFFECT21, ctx -> {
            if(ctx.getCurrentEvent() == null || ctx.getCurrentEvent().getSubType()!=SubType.SUSTENANCE) return;
            Player player = ctx.getActivePlayer();
            int gatherers = player.countCharactersByType(SubType.GATHERER);
            if (gatherers > 0) {
                player.addSustenanceDiscount(gatherers);
            }
        });


    }

    /**
     * Create building building card.
     *
     * @param dto the dto
     * @return the building card
     */
    public static BuildingCard createBuilding(CardDataDTO.BuildingDTO dto) {
        // Controllo validazione dati (Fail-Fast) dopo la prima volta eliminabile l'if
        if (dto.triggerType == null) {
            throw new IllegalArgumentException("Corrupted JSON! Missing TriggerType for Building with ID: " + dto.id);
        }
        TriggerType trigger = TriggerType.valueOf(dto.triggerType);
        //dopo la prima volta si può togliere questo if
        if (dto.EffectID == null) {
            throw new IllegalArgumentException("Corrupted JSON! Missing EffectID for Building with ID:" + dto.id);
        }
        EffectID effectId = EffectID.valueOf(dto.EffectID);
        BuildingEffect effect = effectRegistry.get(effectId);
        return new BuildingCard(dto.id, dto.era, dto.cost, dto.pp, trigger, effect);
    }

}
