package it.polimi.ingsw.am46.view.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Static dictionary to decouple Card IDs from their UI descriptions.
 * Converts raw GameState integer IDs into human-readable English text.
 */
public class CardDictionary {

    private static final Map<Integer, String> descriptions = new HashMap<>();
    private static final Map<Integer, Integer> buildingCosts = new HashMap<>();

    static {
        // ==========================================
        // CHARACTERS (IDs 1 - 84)
        // ==========================================

        // Era 1
        descriptions.put(1, "Character: HUNTER (Era 1) - Provides food");
        descriptions.put(2, "Character: HUNTER (Era 1) - Provides food");
        descriptions.put(3, "Character: HUNTER (Era 1) - Does not provide food");
        descriptions.put(4, "Character: HUNTER (Era 1) - Does not provide food");
        descriptions.put(5, "Character: HUNTER (Era 1) - Does not provide food");
        descriptions.put(6, "Character: BUILDER (Era 1) - PP: 3, Discount: 1");
        descriptions.put(7, "Character: BUILDER (Era 1) - PP: 0, Discount: 2");
        descriptions.put(8, "Character: BUILDER (Era 1) - PP: 1, Discount: 2");
        descriptions.put(9, "Character: BUILDER (Era 1) - PP: 2, Discount: 1");
        descriptions.put(10, "Character: GATHERER (Era 1)");
        descriptions.put(11, "Character: GATHERER (Era 1)");
        descriptions.put(12, "Character: GATHERER (Era 1)");
        descriptions.put(13, "Character: GATHERER (Era 1)");
        descriptions.put(14, "Character: ARTIST (Era 1)");
        descriptions.put(15, "Character: ARTIST (Era 1)");
        descriptions.put(16, "Character: ARTIST (Era 1)");
        descriptions.put(17, "Character: ARTIST (Era 1)");
        descriptions.put(18, "Character: ARTIST (Era 1)");
        descriptions.put(19, "Character: INVENTOR (Era 1) - Item: SHIELD");
        descriptions.put(20, "Character: INVENTOR (Era 1) - Item: BOAT");
        descriptions.put(21, "Character: INVENTOR (Era 1) - Item: ARROW");
        descriptions.put(22, "Character: INVENTOR (Era 1) - Item: BREAD");
        descriptions.put(23, "Character: INVENTOR (Era 1) - Item: ROPE");
        descriptions.put(24, "Character: INVENTOR (Era 1) - Item: FLUTE");
        descriptions.put(25, "Character: INVENTOR (Era 1) - Item: BOWL");
        descriptions.put(26, "Character: SHAMAN (Era 1) - Stars: 2");
        descriptions.put(27, "Character: SHAMAN (Era 1) - Stars: 2");
        descriptions.put(28, "Character: SHAMAN (Era 1) - Stars: 1");
        descriptions.put(29, "Character: SHAMAN (Era 1) - Stars: 1");

        // Era 2
        descriptions.put(30, "Character: HUNTER (Era 2) - Does not provide food");
        descriptions.put(31, "Character: HUNTER (Era 2) - Does not provide food");
        descriptions.put(32, "Character: HUNTER (Era 2) - Provides food");
        descriptions.put(33, "Character: HUNTER (Era 2) - Provides food");
        descriptions.put(34, "Character: HUNTER (Era 2) - Provides food");
        descriptions.put(35, "Character: HUNTER (Era 2) - Does not provide food");
        descriptions.put(36, "Character: BUILDER (Era 2) - PP: 4, Discount: 1");
        descriptions.put(37, "Character: BUILDER (Era 2) - PP: 1, Discount: 2");
        descriptions.put(38, "Character: BUILDER (Era 2) - PP: 2, Discount: 1");
        descriptions.put(39, "Character: BUILDER (Era 2) - PP: 3, Discount: 2");
        descriptions.put(40, "Character: GATHERER (Era 2)");
        descriptions.put(41, "Character: GATHERER (Era 2)");
        descriptions.put(42, "Character: GATHERER (Era 2)");
        descriptions.put(43, "Character: GATHERER (Era 2)");
        descriptions.put(44, "Character: ARTIST (Era 2)");
        descriptions.put(45, "Character: ARTIST (Era 2)");
        descriptions.put(46, "Character: ARTIST (Era 2)");
        descriptions.put(47, "Character: ARTIST (Era 2)");
        descriptions.put(48, "Character: INVENTOR (Era 2) - Item: IDOL");
        descriptions.put(49, "Character: INVENTOR (Era 2) - Item: HOOK");
        descriptions.put(50, "Character: INVENTOR (Era 2) - Item: ROPE");
        descriptions.put(51, "Character: INVENTOR (Era 2) - Item: FLUTE");
        descriptions.put(52, "Character: INVENTOR (Era 2) - Item: BOWL");
        descriptions.put(53, "Character: INVENTOR (Era 2) - Item: SHIELD");
        descriptions.put(54, "Character: SHAMAN (Era 2) - Stars: 2");
        descriptions.put(55, "Character: SHAMAN (Era 2) - Stars: 2");
        descriptions.put(56, "Character: SHAMAN (Era 2) - Stars: 1");
        descriptions.put(57, "Character: SHAMAN (Era 2) - Stars: 2");

        // Era 3
        descriptions.put(58, "Character: HUNTER (Era 3) - Provides food");
        descriptions.put(59, "Character: HUNTER (Era 3) - Does not provide food");
        descriptions.put(60, "Character: HUNTER (Era 3) - Does not provide food");
        descriptions.put(61, "Character: HUNTER (Era 3) - Provides food");
        descriptions.put(62, "Character: BUILDER (Era 3) - PP: 5, Discount: 1");
        descriptions.put(63, "Character: BUILDER (Era 3) - PP: 3, Discount: 2");
        descriptions.put(64, "Character: BUILDER (Era 3) - PP: 4, Discount: 1");
        descriptions.put(65, "Character: BUILDER (Era 3) - PP: 2, Discount: 2");
        descriptions.put(66, "Character: GATHERER (Era 3)");
        descriptions.put(67, "Character: GATHERER (Era 3)");
        descriptions.put(68, "Character: GATHERER (Era 3)");
        descriptions.put(69, "Character: ARTIST (Era 3)");
        descriptions.put(70, "Character: ARTIST (Era 3)");
        descriptions.put(71, "Character: ARTIST (Era 3)");
        descriptions.put(72, "Character: ARTIST (Era 3)");
        descriptions.put(73, "Character: INVENTOR (Era 3) - Item: NECKLACE");
        descriptions.put(74, "Character: INVENTOR (Era 3) - Item: BOAT");
        descriptions.put(75, "Character: INVENTOR (Era 3) - Item: ARROW");
        descriptions.put(76, "Character: INVENTOR (Era 3) - Item: IDOL");
        descriptions.put(77, "Character: INVENTOR (Era 3) - Item: HOOK");
        descriptions.put(78, "Character: INVENTOR (Era 3) - Item: NECKLACE");
        descriptions.put(79, "Character: INVENTOR (Era 3) - Item: BREAD");
        descriptions.put(80, "Character: SHAMAN (Era 3) - Stars: 2");
        descriptions.put(81, "Character: SHAMAN (Era 3) - Stars: 3");
        descriptions.put(82, "Character: SHAMAN (Era 3) - Stars: 2");
        descriptions.put(83, "Character: SHAMAN (Era 3) - Stars: 3");
        descriptions.put(84, "Character: SHAMAN (Era 3) - Stars: 2");


        // ==========================================
        // EVENTS (IDs 85 - 96)
        // ==========================================

        descriptions.put(85, "Event: HUNT (Era 1) - Grants 1 PP per Hunter");
        descriptions.put(86, "Event: SUSTENANCE (Era 1) - Penalty: 1 PP");
        descriptions.put(87, "Event: SHAMANIC RITUAL (Era 1) - Win: 5 PP, Lose: 3 PP");
        descriptions.put(88, "Event: CAVE PAINTINGS (Era 1) - Requires min. 1 Artist. Penalty: 2 PP, Reward: 1 PP per Artist");
        descriptions.put(89, "Event: HUNT (Era 2) - Grants 2 PP per Hunter");
        descriptions.put(90, "Event: SUSTENANCE (Era 2) - Penalty: 2 PP");
        descriptions.put(91, "Event: SHAMANIC RITUAL (Era 2) - Win: 10 PP, Lose: 5 PP");
        descriptions.put(92, "Event: CAVE PAINTINGS (Era 2) - Requires min. 2 Artists. Penalty: 2 PP, Reward: 2 PP per Artist");
        descriptions.put(93, "Event: HUNT (Era 3) - Grants 3 PP per Hunter");
        descriptions.put(94, "Event: CAVE PAINTINGS (Era 3) - Requires min. 3 Artists. Penalty: 2 PP, Reward: 3 PP per Artist");
        descriptions.put(95, "Event: SUSTENANCE (Era 3) - Penalty: 3 PP [Final Event]");
        descriptions.put(96, "Event: SHAMANIC RITUAL (Era 3) - Win: 15 PP, Lose: 7 PP [Final Event]");


        // ==========================================
        // BUILDINGS (IDs 97 - 117)
        // ==========================================

        // ==========================================
        // BUILDINGS (IDs 97 - 117)
        // ==========================================

        descriptions.put(97, "Building (Era 1) - Cost: 4, PP: 3. Effect: Earn 5 food each time you complete a new set of characters.");
        descriptions.put(98, "Building (Era 1) - Cost: 4, PP: 4. Effect: Each Gatherer provides 1 food discount during Sustenance events.");
        descriptions.put(99, "Building (Era 1) - Cost: 5, PP: 3. Effect: Each Artist provides 1 food discount during Sustenance events.");
        descriptions.put(100, "Building (Era 1) - Cost: 5, PP: 2. Effect: Grants immunity to PP loss during Shaman Ritual (SHR) events.");
        descriptions.put(101, "Building (Era 1) - Cost: 3, PP: 3. Effect: Earn 1 extra food when placing your Totem on a space that provides food.");
        descriptions.put(102, "Building (Era 1) - Cost: 4, PP: 3. Effect: Earn 3 food each time you complete a new pair of Inventors.");
        descriptions.put(103, "Building (Era 2) - Cost: 7, PP: 0. Effect: Double the PP gained if you win a Shaman Ritual (SHR) event.");
        descriptions.put(104, "Building (Era 2) - Cost: 6, PP: 4. Effect: Adds 3 temporary Shaman stars during Shaman Ritual (SHR) events.");
        descriptions.put(105, "Building (Era 2) - Cost: 7, PP: 4. Effect: Each Inventor provides 1 food discount during Sustenance events.");
        descriptions.put(106, "Building (Era 2) - Cost: 7, PP: 2. Effect: During Hunt events, earn 1 extra food and 1 extra PP per Hunter.");
        descriptions.put(107, "Building (Era 2) - Cost: 6, PP: 4. Effect: At the end of the game, double the PP provided by your Builders.");
        descriptions.put(108, "Building (Era 2) - Cost: 5, PP: 6. Effect: Earn 1 food per Artist during Cave Painting events.");
        descriptions.put(109, "Building (Era 2) - Cost: 5, PP: 6. Effect: Earn 6 PP for each complete set of characters at the end of the game.");
        descriptions.put(110, "Building (Era 3) - Cost: 8, PP: 8. Effect: Earn 3 PP for each Hunter at the end of the game.");
        descriptions.put(111, "Building (Era 3) - Cost: 7, PP: 6. Effect: Earn 4 PP for each Gatherer at the end of the game.");
        descriptions.put(112, "Building (Era 3) - Cost: 7, PP: 4. Effect: Earn 4 PP for each Shaman at the end of the game.");
        descriptions.put(113, "Building (Era 3) - Cost: 6, PP: 3. Effect: Earn 4 PP for each Builder at the end of the game.");
        descriptions.put(114, "Building (Era 3) - Cost: 7, PP: 4. Effect: Earn 4 PP for each Artist at the end of the game.");
        descriptions.put(115, "Building (Era 3) - Cost: 6, PP: 6. Effect: Earn 2 PP for each Inventor at the end of the game.");
        descriptions.put(116, "Building (Era 3) - Cost: 9, PP: 3. Effect: Allows you to draw an extra card at the end of the round.");
        descriptions.put(117, "Building (Era 3) - Cost: 10, PP: 0. Effect: Earn 25 PP at the end of the game.");


        buildingCosts.put(97, 4); buildingCosts.put(98, 4); buildingCosts.put(99, 5);
        buildingCosts.put(100, 5); buildingCosts.put(101, 3); buildingCosts.put(102, 4);
        buildingCosts.put(103, 7); buildingCosts.put(104, 6); buildingCosts.put(105, 7);
        buildingCosts.put(106, 7); buildingCosts.put(107, 6); buildingCosts.put(108, 5);
        buildingCosts.put(109, 5); buildingCosts.put(110, 8); buildingCosts.put(111, 7);
        buildingCosts.put(112, 7); buildingCosts.put(113, 6); buildingCosts.put(114, 7);
        buildingCosts.put(115, 6); buildingCosts.put(116, 9); buildingCosts.put(117, 10);
    }

    /**
     * Retrieves the human-readable description for a given card ID.
     * @param id The integer ID of the card.
     * @return The formatted string description, or a fallback message if ID is unknown.
     */
    public static String getCardInfo(int id) {
        return descriptions.getOrDefault(id, "Unknown Card (ID: " + id + ")");
    }

    public static String getCardType(int id) {
        if (id >= 97 && id <= 117) return "BUILDING";
        if (id >= 85 && id <= 96) return "EVENT";

        // Se non è Building o Event, è un Personaggio. Controlliamo i range delle Ere.
        if (isBetween(id, 1, 5) || isBetween(id, 30, 35) || isBetween(id, 58, 61)) return "HUNTER";
        if (isBetween(id, 6, 9) || isBetween(id, 36, 39) || isBetween(id, 62, 65)) return "BUILDER";
        if (isBetween(id, 10, 13) || isBetween(id, 40, 43) || isBetween(id, 66, 68)) return "GATHERER";
        if (isBetween(id, 14, 18) || isBetween(id, 44, 47) || isBetween(id, 69, 72)) return "ARTIST";
        if (isBetween(id, 19, 25) || isBetween(id, 48, 53) || isBetween(id, 73, 79)) return "INVENTOR";
        if (isBetween(id, 26, 29) || isBetween(id, 54, 57) || isBetween(id, 80, 84)) return "SHAMAN";

        return "UNKNOWN";
    }

    /**
     * Extracts a clean, human-readable name for the card based on its ID.
     */

    public static String getCardName(int id) {
        String info = getCardInfo(id);


        if (info.startsWith("Character: ")) {
            return info.substring(11).trim();
        }

        if (info.startsWith("Event: ")) {
            return info.substring(7).trim();
        }

        if (info.startsWith("Building ")) {
            String era = info.substring(10).trim();
            return "BUILDING (" + era + ")";
        }

        return info;
    }


    private static boolean isBetween(int id, int min, int max) {
        return id >= min && id <= max;
    }


    public static int getCardCost(int id) {
        return buildingCosts.getOrDefault(id, 0);
    }
}