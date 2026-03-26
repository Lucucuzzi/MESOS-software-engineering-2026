package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

import java.util.HashMap;
import java.util.Map;

public class BuildingFactory {
    private static final Map<EffectID, BuildingEffect> effectRegistry = new HashMap<>();

    // Il blocco static inizializza la mappa una sola volta all'avvio del server
    static {
        // Esempio 1: Edificio che fornisce 25 PP a fine partita [cite: 272]
        effectRegistry.put(EffectID.EFFECT1, ctx -> {
            System.out.println("Executing 25 PP effect...");
            ctx.getActivePlayer().modifyPP(25);
        });

        // Esempio 2: Sconto durante l'evento Sostentamento (1 per ogni artista/inventore/raccoglitore)
        effectRegistry.put(EffectID.EFFECT2, ctx -> {
            System.out.println("Applying sustain discount for Artists, Inventor, Gatherer...");
            Player player = ctx.getActivePlayer();
            int artists = player.countCharactersByType(SubType.ARTIST);
            int inventors = player.countCharactersByType(SubType.INVENTOR);
            int gatherers = player.countCharactersByType(SubType.GATHERER);
            int totalDiscount = artists + inventors + gatherers;
            if (totalDiscount > 0) {
                int currentDiscount = player.getSustenanceDiscount();
                player.addSustenanceDiscount(currentDiscount + totalDiscount);
            }
        });

        // Esempio 3: Prendi 3 cibo ogni volta che ottieni una coppia di Inventori [cite: 259]
        effectRegistry.put(EffectID.EFFECT3, ctx -> {
            System.out.println("Checking inventor pairs for 3 Food...");
            // Logica di controllo sull'inventario del giocatore la logica di controllo
            // non va qui, ma nel game quando fa addCard che dovra triggerare
            //questo effetto se le coopie di inventori aumentano!
            //ctx.getCurrentPlayer().modifyFood(3);

        });
        effectRegistry.put(EffectID.EFFECT4, ctx -> {
            System.out.println("Applying hunt bonus: +1 food and +1 PP per hunter...");
            Player p = ctx.getActivePlayer();
            int hunters = p.countCharactersByType(SubType.HUNTER);
            p.modifyFood(hunters);
            p.modifyPP(hunters);
        });

        // Sciamanico → immunità perdita PP
        effectRegistry.put(EffectID.EFFECT5, ctx -> {
            System.out.println("Applying shaman immunity to PP loss...");
            ctx.getActivePlayer().setShamanImmunity(true);
        });

        // Sciamanico → +3 icone
        effectRegistry.put(EffectID.EFFECT6, ctx -> {
            System.out.println("Adding 3 extra shaman icons...");
            ctx.getActivePlayer().addExtraShamanIcons();
        });

        // Sciamanico → doppio PP se più icone di tutti
        effectRegistry.put(EffectID.EFFECT7, ctx -> {
            System.out.println("Enabling double PP if shaman icons majority...");
            ctx.getActivePlayer().setShamanDoublePP(true);
        });

        // Pitture Rupestri → 1 Cibo per Artista
        effectRegistry.put(EffectID.EFFECT8, ctx -> {
            System.out.println("Applying cave art food bonus per artist...");
            Player p = ctx.getActivePlayer();
            p.modifyFood(p.countCharactersByType(SubType.ARTIST));
        });

        // Set completo → 5 Cibo
        effectRegistry.put(EffectID.EFFECT9, ctx -> {
            System.out.println("Complete set formed, giving 5 food...");
            //ctx.getCurrentPlayer().modifyFood(5);
        });

        // Totem su spazio bonus → 1 Cibo extra
        effectRegistry.put(EffectID.EFFECT10, ctx -> {
            //System.out.println("Totem on bonus space, giving 1 extra food...");
            //ctx.getCurrentPlayer().modifyFood(1);
        });

        // Carta extra prima del Fine Round
        effectRegistry.put(EffectID.EFFECT11, ctx -> {
            System.out.println("Enabling extra card before end round...");
            //ctx.getCurrentPlayer().setCanTakeExtraCard(true);
        });

        // Doppio PP Costruttori a fine partita
        effectRegistry.put(EffectID.EFFECT12, ctx -> {
            System.out.println("Applying double builder PP at end game...");
            //Player p = ctx.getCurrentPlayer();
            //p.modifyPP(p.calculateBuilderPP());
        });

        // 6 PP per ogni set completo a fine partita
        effectRegistry.put(EffectID.EFFECT13, ctx -> {
            System.out.println("Applying 6 PP per complete set at end game...");
            Player p = ctx.getActivePlayer();
            p.modifyPP(p.countCompleteSets() * 6);
        });

        // PP per ogni carta del tipo indicato a fine partita (ne vanno implementate di più)
        effectRegistry.put(EffectID.EFFECT14, ctx -> {
            System.out.println("Applying PP per character type at end game...");
            //Player p = ctx.getCurrentPlayer();
            //p.modifyPP(p.countCharactersByType(SUBTYPE.ARTIST) * 3);
        });

        /*
        bisogna aggiungere al ctx interfsce il corruentround, cosi che nelle lambda
        che si attivano a fine partita (trigger == END_TURN), si mette un if currentround==10
         */


    }

    public static BuildingCard createBuilding(int id, int era, int cost, int pp, int food, TriggerType triggerType, EffectID effectId) {

        // 1. Recupero la Strategy (l'effetto) dalla Dispatch Table
        BuildingEffect effect = effectRegistry.get(effectId);

        /* 2. Controllo difensivo: se l'ID letto dal JSON non esiste, blocco l'esecuzione
        if (effect == null) {
            throw new IllegalArgumentException("Unknown effect ID: " + effectId);
        }*/

        // 3. Istanzio e ritorno la carta completa
        return new BuildingCard(id, era, cost, pp, food, triggerType, effect);
    }

}
