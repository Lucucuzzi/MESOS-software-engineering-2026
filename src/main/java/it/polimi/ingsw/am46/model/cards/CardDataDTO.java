package it.polimi.ingsw.am46.model.cards;

import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingEffect;

import java.util.List;

public class CardDataDTO {
    public List<CharacterDTO> characters;
    public List<EventDTO> events;
    public List<BuildingDTO> buildings;

    // --- SOTTOCLASSI INTERNE STATICHE --- //

    public static class CharacterDTO {
        public Integer id;
        public Integer era;
        public int cost;
        public String SubType;
        public int minPlayers;

        //FAT DTO
        public Integer pp;
        public Integer discount;
        public Integer food;
        public String item;
        public Integer stars;
    }

    public static class EventDTO {
        public Integer id;
        public Integer era;
        public int cost;
        public String subtype;
        public Boolean finalEvent;

        public Integer ppPenalty;
        public Integer minArtistRequired;

        public Integer ppRewardArtist;
        public Integer ppHunter;
        public Integer winPP;
        public Integer losePP;
    }

    public static class BuildingDTO {
        public int pp;
        public int food;
        public String triggerType;
        public String EffectID;
    }
}