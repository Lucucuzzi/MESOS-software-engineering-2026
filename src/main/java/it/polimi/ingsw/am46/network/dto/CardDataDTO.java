package it.polimi.ingsw.am46.network.dto;

import java.util.List;

/**
 * The type Card data dto.
 */
public class CardDataDTO {
    /**
     * The Characters.
     */
    public List<CharacterDTO> characters;
    /**
     * The Events.
     */
    public List<EventDTO> events;
    /**
     * The Buildings.
     */
    public List<BuildingDTO> buildings;

    // --- SOTTOCLASSI INTERNE STATICHE --- //

    /**
     * The type Character dto.
     */
    public static class CharacterDTO {
        /**
         * The Id.
         */
        public int id;
        /**
         * The Era.
         */
        public int era;
        /**
         * The Cost.
         */
        public int cost;
        /**
         * The Sub type.
         */
        public String SubType;
        /**
         * The Min players.
         */
        public int minPlayers;

        /**
         * The Pp.
         */
//FAT DTO
        public int pp;
        /**
         * The Discount.
         */
        public int discount;
        /**
         * The Food.
         */
        public boolean food;
        /**
         * The Item.
         */
        public String item;
        /**
         * The Stars.
         */
        public int stars;
    }

    /**
     * The type Event dto.
     */
    public static class EventDTO {
        /**
         * The Id.
         */
        public int id;
        /**
         * The Era.
         */
        public int era;
        /**
         * The Cost.
         */
        public int cost;
        /**
         * The Sub type.
         */
        public String SubType;
        /**
         * The Final event.
         */
        public boolean finalEvent;

        /**
         * The Pp penalty.
         */
        public int ppPenalty;
        /**
         * The Min artist required.
         */
        public int minArtistRequired;

        /**
         * The Pp reward artist.
         */
        public int ppRewardArtist;
        /**
         * The Pp hunter.
         */
        public int ppHunter;
        /**
         * The Win pp.
         */
        public int winPP;
        /**
         * The Lose pp.
         */
        public int losePP;
    }

    /**
     * The type Building dto.
     */
    public static class BuildingDTO {
        /**
         * The Id.
         */
        public int id;
        /**
         * The Era.
         */
        public int era;
        /**
         * The Cost.
         */
        public int cost;
        /**
         * The Pp.
         */
        public int pp;
        /**
         * The Trigger type.
         */
        public String triggerType;
        /**
         * The Effect id.
         */
        public String EffectID;
        /**
         * The Description.
         */
        public String description;
    }
}