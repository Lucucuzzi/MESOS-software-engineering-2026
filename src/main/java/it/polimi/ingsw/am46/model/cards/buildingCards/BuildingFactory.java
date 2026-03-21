package it.polimi.ingsw.am46.model.cards.buildingCards;

import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.enums.EffectID;

import java.util.HashMap;
import java.util.Map;

public class BuildingFactory {
    private static final Map<EffectID, BuildingEffect> effectRegistry = new HashMap<>();

    // Il blocco static inizializza la mappa una sola volta all'avvio del server
    static {
        // Esempio 1: Edificio che fornisce 25 PP a fine partita [cite: 272]
        effectRegistry.put(EffectID.EFFECT1, ctx -> {
            System.out.println("Executing 25 PP effect...");
            // ctx.getCurrentPlayer().addPrestigePoints(25);
        });

        // Esempio 2: Sconto durante l'evento Sostentamento per gli Artisti [cite: 245, 246]
        effectRegistry.put(EffectID.EFFECT2, ctx -> {
            System.out.println("Applying sustain discount for Artists...");
            // ctx.getCurrentPlayer().addSustainDiscount(1, CardType.ARTIST);
        });

        // Esempio 3: Prendi 3 cibo ogni volta che ottieni una coppia di Inventori [cite: 259]
        effectRegistry.put(EffectID.EFFECT3, ctx -> {
            System.out.println("Checking inventor pairs for 3 Food...");
            // Logica di controllo sull'inventario del giocatore
        });
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
