package it.polimi.ingsw.am46.network.dto;

import java.util.List;

public class CardDataDTO {
    public List<CharacterDTO> characters;
    public List<EventDTO> events;
    public List<BuildingDTO> buildings;

    // --- SOTTOCLASSI INTERNE STATICHE --- //

    public static class CharacterDTO {
        public int id;
        public int era;
        public int cost;
        public String SubType;
        public int minPlayers;

        //FAT DTO
        public int pp;
        public int discount;
        public boolean food;
        public String item;
        public int stars;
    }

    public static class EventDTO {
        public int id;
        public int era;
        public int cost;
        public String SubType;
        public boolean finalEvent;

        public int ppPenalty;
        public int minArtistRequired;

        public int ppRewardArtist;
        public int ppHunter;
        public int winPP;
        public int losePP;
    }

    public static class BuildingDTO {
        public int id;
        public int era;
        public int cost;
        public int pp;
        public String triggerType;
        public String EffectID;
        public String description;
    }
}