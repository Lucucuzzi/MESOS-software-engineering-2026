package it.polimi.ingsw.am46.view.utils;

import com.google.gson.Gson;
import it.polimi.ingsw.am46.network.dto.CardDataDTO;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Card dictionary.
 */
public class CardDictionary {

    private static class CardData {
        /**
         * The Type.
         */
        final String type;
        /**
         * The Detail.
         */
        final String detail;
        /**
         * The Era.
         */
        final int era;
        /**
         * The Building cost.
         */
        final int buildingCost;
        /**
         * The Building pp.
         */
        final int buildingPP;

        /**
         * Instantiates a new Card data.
         *
         * @param type         the type
         * @param detail       the detail
         * @param era          the era
         * @param buildingCost the building cost
         * @param buildingPP   the building pp
         */
        CardData(String type, String detail, int era, int buildingCost, int buildingPP) {
            this.type = type;
            this.detail = detail;
            this.era = era;
            this.buildingCost = buildingCost;
            this.buildingPP = buildingPP;
        }
    }

    private static final Map<Integer, CardData> cards = new HashMap<>();

    static {
        try (InputStream is = CardDictionary.class
                .getClassLoader()
                .getResourceAsStream("cards.json")) {

            if (is == null) throw new RuntimeException("cards.json not found in resources");

            CardDataDTO data = new Gson().fromJson(new InputStreamReader(is), CardDataDTO.class);

            for (CardDataDTO.CharacterDTO c : data.characters) {
                cards.put(c.id, new CardData(
                        c.SubType.toUpperCase(),
                        buildCharacterDetail(c),
                        c.era, 0, 0
                ));
            }

            for (CardDataDTO.EventDTO e : data.events) {
                int era = e.finalEvent ? 0 : e.era;
                cards.put(e.id, new CardData(
                        "EVENT",
                        mapEventName(e.SubType) + "\n" + buildEventDetail(e),
                        era, 0, 0
                ));
            }

            for (CardDataDTO.BuildingDTO b : data.buildings) {
                cards.put(b.id, new CardData(
                        "BUILDING",
                        b.description != null ? b.description : "",
                        b.era, b.cost, b.pp
                ));
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to load cards.json: " + ex.getMessage(), ex);
        }
    }

    private static String buildCharacterDetail(CardDataDTO.CharacterDTO c) {
        return switch (c.SubType.toUpperCase()) {
            case "HUNTER"   -> c.food ? "Provides food" : "No food";
            case "BUILDER"  -> "Discount: " + c.discount + "\nPrestige: " + c.pp + " PP";
            case "GATHERER" -> "Discount: 3";
            case "INVENTOR" -> "Item: " + (c.item != null ? c.item : "?");
            case "SHAMAN"   -> "Stars: " + c.stars;
            default         -> "";
        };
    }

    private static String buildEventDetail(CardDataDTO.EventDTO e) {
        return switch (e.SubType.toUpperCase()) {
            case "HUNT"       -> "Grants " + e.ppHunter + " PP per Hunter.";
            case "SUSTENANCE" -> "Penalty: " + e.ppPenalty + " PP per member without food.";
            case "SHR"        -> "Win: +" + e.winPP + " PP. Lose: -" + e.losePP + " PP.";
            case "CAVEP"      -> "Min. " + e.minArtistRequired + " Artist(s). " +
                    "Penalty: " + e.ppPenalty + " PP. " +
                    "Reward: " + e.ppRewardArtist + " PP per Artist.";
            default           -> "";
        };
    }

    private static String mapEventName(String subType) {
        return switch (subType.toUpperCase()) {
            case "HUNT"       -> "HUNT";
            case "SUSTENANCE" -> "SUSTENANCE";
            case "SHR"        -> "SHAMANIC RITUAL";
            case "CAVEP"      -> "CAVE PAINTINGS";
            default           -> subType;
        };
    }

    /**
     * Gets card type.
     *
     * @param id the id
     * @return the card type
     */
    public static String getCardType(int id) {
        CardData c = cards.get(id);
        return c != null ? c.type : "UNKNOWN";
    }

    /**
     * Gets card name.
     *
     * @param id the id
     * @return the card name
     */
    public static String getCardName(int id) {
        CardData c = cards.get(id);
        if (c == null) return "Unknown Card";
        if ("EVENT".equals(c.type)) return c.detail.split("\n")[0];
        return c.type;
    }

    /**
     * Gets card era.
     *
     * @param id the id
     * @return the card era
     */
    public static String getCardEra(int id) {
        CardData c = cards.get(id);
        if (c == null) return "";
        return c.era == 0 ? "FINAL EVENT" : "Era " + c.era;
    }

    /**
     * Gets building cost.
     *
     * @param id the id
     * @return the building cost
     */
    public static int getBuildingCost(int id) {
        CardData c = cards.get(id);
        return c != null ? c.buildingCost : 0;
    }

    /**
     * Gets building pp.
     *
     * @param id the id
     * @return the building pp
     */
    public static int getBuildingPP(int id) {
        CardData c = cards.get(id);
        return c != null ? c.buildingPP : 0;
    }

    /**
     * Gets card detail.
     *
     * @param id the id
     * @return the card detail
     */
    public static String getCardDetail(int id) {
        CardData c = cards.get(id);
        if (c == null) return "";
        if ("EVENT".equals(c.type)) {
            String[] parts = c.detail.split("\n", 2);
            return parts.length > 1 ? parts[1] : "";
        }
        return c.detail;
    }
}