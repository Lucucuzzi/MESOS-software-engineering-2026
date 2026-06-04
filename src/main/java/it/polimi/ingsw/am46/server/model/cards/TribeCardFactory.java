package it.polimi.ingsw.am46.server.model.cards;

import it.polimi.ingsw.am46.server.model.cards.CardDataDTO.CharacterDTO;
import it.polimi.ingsw.am46.server.model.cards.CardDataDTO.EventDTO;
import it.polimi.ingsw.am46.model.cards.characterCards.*;
import it.polimi.ingsw.am46.server.model.cards.characterCards.*;
import it.polimi.ingsw.am46.server.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.eventCards.*;
import it.polimi.ingsw.am46.server.model.cards.eventCards.*;

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


            if (dto.item == null) {
                throw new IllegalArgumentException("Error JSON: The 'item' attribute is missing for the Inventor '" + dto.id + "'");
            }
            Item i;
            try {
                i = Item.valueOf(dto.item.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Errore JSON: L'item '" + dto.item + "' non è valido per l'Inventore '" + dto.id + "'");
            }

            return new Inventor(dto.id, dto.era, dto.cost, i, dto.minPlayers);
        });

        charRegistry.put("SHAMAN", dto -> {
            return new Shaman(dto.id, dto.era, dto.cost, dto.stars, dto.minPlayers);
        });
    }

    static {
        // --- REGISTRY EVENT ---

        eventRegistry.put("CAVEP", dto -> {
            return new CavePaintings(dto.id, dto.era, dto.cost, dto.finalEvent, dto.minArtistRequired, dto.ppPenalty, dto.ppRewardArtist);
        });

        eventRegistry.put("HUNT", dto -> {
            return new Hunt(dto.id, dto.era, dto.cost, dto.finalEvent, dto.ppHunter);
        });

        eventRegistry.put("SHR", dto -> {
            return new ShamanicRitual(dto.id, dto.era, dto.cost, dto.finalEvent, dto.winPP, dto.losePP);
        });

        eventRegistry.put("SUSTENANCE", dto -> {
            return new Sustenance(dto.id, dto.era, dto.cost, dto.finalEvent, dto.ppPenalty);
        });
    }

    public static CharacterCard createCharacter(CharacterDTO dto) {
        CharacterConstructor constructor = charRegistry.get(dto.SubType.toUpperCase());

        if (constructor == null) {
            throw new IllegalArgumentException("Unknown subtype: " + dto.SubType);
        }
        return constructor.create(dto);
    }


    public static EventCard createEvent(EventDTO dto) {
        EventConstructor constructor = eventRegistry.get(dto.SubType.toUpperCase());

        //controllo che si può togliere dopo che carichiamo per la prima volta tutte le carte, serve solo per
        //controllare di non aver fatto errori di scrittura nel subType nel Json
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown subtype: " + dto.SubType);
        }
        return constructor.create(dto);
    }
}