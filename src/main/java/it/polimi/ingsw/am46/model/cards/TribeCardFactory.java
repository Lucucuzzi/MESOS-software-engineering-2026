package it.polimi.ingsw.am46.model.cards;

import it.polimi.ingsw.am46.model.cards.CardDataDTO.CharacterDTO;
import it.polimi.ingsw.am46.model.cards.CardDataDTO.EventDTO;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.*;

import java.util.HashMap;
import java.util.Map;

public class TribeCardFactory {

    @FunctionalInterface
    public interface CharacterConstructor {
        CharacterCard create(CharacterDTO dto);
    }

    @FunctionalInterface
    public interface EventConstructor {
         EventCard create(EventDTO dto);
    }


    private static final Map<String, CharacterConstructor> charRegistry = new HashMap<>();
    private static final Map<String, EventConstructor> eventRegistry = new HashMap<>();

    static {
        // --- REGISTRY CHARACTER ---

        charRegistry.put("ARTIST", dto -> {
            return new Artist(dto.id, dto.era, dto.cost, dto.minPlayers);
        });

        charRegistry.put("BUILDER", dto -> {
            return new Builder(dto.id, dto.era, dto.cost, dto.pp, dto.discount, dto.minPlayers);
        });

        charRegistry.put("GATHERER", dto -> {
            return new Gatherer(dto.id, dto.era, dto.cost, dto.minPlayers);
        });

        charRegistry.put("HUNTER", dto -> {
            return new Hunter(dto.id, dto.era, dto.cost, dto.food, dto.minPlayers);
        });

        charRegistry.put("INVENTOR", dto -> {

            // 1. Controllo anti-amnesia (Hai scordato l'attributo nel JSON?) si può togliere dopo che il primo
            //caricamento ha successo. Cancella tutto il blocco a parte quello finale
            if (dto.item == null) {
                throw new IllegalArgumentException("Errore JSON: Manca l'attributo 'item' per l'Inventore '" + dto.id + "'");
            }
            Item i;
            try {
                // 2. Proviamo a convertirlo
                i = Item.valueOf(dto.item.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 3. Controllo anti-typo (Hai scritto "LENGNO" invece di "LEGNO"?)
                throw new IllegalArgumentException("Errore JSON: L'item '" + dto.item + "' non è valido per l'Inventore '" + dto.id + "'");
            }

            //Item i = Item.valueOf(dto.item.toUpperCase());
            return new Inventor(dto.id, dto.era, dto.cost, i, dto.minPlayers);
        });

        charRegistry.put("SHAMAN", dto -> {
            return new Shaman(dto.id, dto.era, dto.cost, dto.stars, dto.minPlayers);
        });
    }

    static {
        // --- REGISTRY EVENT ---

        eventRegistry.put("CAVE_PAINTINGS", dto -> {
            return new CavePaintings(dto.id, dto.era, dto.cost, dto.finalEvent, dto.minArtistRequired, dto.ppPenalty, dto.ppRewardArtist);
        });

        eventRegistry.put("HUNT", dto -> {
            return new Hunt(dto.id, dto.era, dto.cost, dto.finalEvent, dto.ppHunter);
        });

        eventRegistry.put("SHAMANIC_RITUAL", dto -> {
            return new ShamanicRitual(dto.id, dto.era, dto.cost, dto.finalEvent, dto.winPP, dto.losePP);
        });

        eventRegistry.put("SUSTENANCE", dto -> {
            return new Sustenance(dto.id, dto.era, dto.cost, dto.finalEvent, dto.ppPenalty);
        });
    }

    public static CharacterCard createCharacter(CharacterDTO dto) {
        CharacterConstructor constructor = charRegistry.get(dto.SubType.toUpperCase());

        //controllo che si può togliere dopo che carichiamo per la prima volta tutte le carte, serve solo per
        //controllare di non aver fatto errori di scrittura nel subType nel Json
        if (constructor == null) {
            throw new IllegalArgumentException("Sottotipo personaggio sconosciuto: " + dto.SubType);
        }
        return constructor.create(dto);
    }


    public static EventCard createEvent(EventDTO dto) {
        EventConstructor constructor = eventRegistry.get(dto.SubType.toUpperCase());

        //controllo che si può togliere dopo che carichiamo per la prima volta tutte le carte, serve solo per
        //controllare di non aver fatto errori di scrittura nel subType nel Json
        if (constructor == null) {
            throw new IllegalArgumentException("Sottotipo evento sconosciuto: " + dto.SubType);
        }
        return constructor.create(dto);
    }
}