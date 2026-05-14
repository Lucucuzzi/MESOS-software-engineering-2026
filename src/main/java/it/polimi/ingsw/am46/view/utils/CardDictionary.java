package it.polimi.ingsw.am46.view.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Static dictionary to decouple Card IDs from their UI descriptions.
 * Converts raw GameState integer IDs into human-readable English text.
 */
public class CardDictionary {

    private static class CardData {
        final String type;
        final String detail;
        final int era;
        final int buildingCost;
        final int buildingPP;

        CardData(String type, String detail, int era, int buildingCost, int buildingPP) {
            this.type = type;
            this.detail = detail;
            this.era = era;
            this.buildingCost = buildingCost;
            this.buildingPP = buildingPP;
        }
    }

    private static final Map<Integer, CardData> cards = new HashMap<>();

    private static void h(int id, int era, boolean food) {
        cards.put(id, new CardData("HUNTER", food ? "Provides food" : "No food", era, 0, 0));
    }
    private static void b(int id, int era, int pp, int discount) {
        cards.put(id, new CardData("BUILDER", "Discount: " + discount + "\nPrestige: " + pp + " PP", era, 0, 0));
    }
    private static void g(int id, int era) {
        cards.put(id, new CardData("GATHERER", "Discount: 3", era, 0, 0));
    }
    private static void a(int id, int era) {
        cards.put(id, new CardData("ARTIST", "", era, 0, 0));
    }
    private static void i(int id, int era, String item) {
        cards.put(id, new CardData("INVENTOR", "Item: " + item, era, 0, 0));
    }
    private static void s(int id, int era, int stars) {
        cards.put(id, new CardData("SHAMAN", "Stars: " + stars, era, 0, 0));
    }
    private static void e(int id, int era, String name, String desc) {
        cards.put(id, new CardData("EVENT", name + "\n" + desc, era, 0, 0));
    }
    private static void bd(int id, int era, int cost, int pp, String effect) {
        cards.put(id, new CardData("BUILDING", effect, era, cost, pp));
    }

    static {
        h(1, 1, true);  h(2, 1, true);  h(3, 1, false); h(4, 1, false); h(5, 1, false);
        h(30, 2, false); h(31, 2, false); h(32, 2, true); h(33, 2, true); h(34, 2, true); h(35, 2, false);
        h(58, 3, true); h(59, 3, false); h(60, 3, false); h(61, 3, true);

        b(6, 1, 3, 1); b(7, 1, 0, 2); b(8, 1, 1, 2); b(9, 1, 2, 1);
        b(36, 2, 4, 1); b(37, 2, 1, 2); b(38, 2, 2, 1); b(39, 2, 3, 2);
        b(62, 3, 5, 1); b(63, 3, 3, 2); b(64, 3, 4, 1); b(65, 3, 2, 2);

        g(10, 1); g(11, 1); g(12, 1); g(13, 1);
        g(40, 2); g(41, 2); g(42, 2); g(43, 2);
        g(66, 3); g(67, 3); g(68, 3);

        a(14, 1); a(15, 1); a(16, 1); a(17, 1); a(18, 1);
        a(44, 2); a(45, 2); a(46, 2); a(47, 2);
        a(69, 3); a(70, 3); a(71, 3); a(72, 3);

        i(19, 1, "SHIELD"); i(20, 1, "BOAT");  i(21, 1, "ARROW"); i(22, 1, "BREAD");
        i(23, 1, "ROPE");   i(24, 1, "FLUTE"); i(25, 1, "BOWL");
        i(48, 2, "IDOL");   i(49, 2, "HOOK");  i(50, 2, "ROPE");  i(51, 2, "FLUTE");
        i(52, 2, "BOWL");   i(53, 2, "SHIELD");
        i(73, 3, "NECKLACE"); i(74, 3, "BOAT");     i(75, 3, "ARROW"); i(76, 3, "IDOL");
        i(77, 3, "HOOK");     i(78, 3, "NECKLACE"); i(79, 3, "BREAD");

        s(26, 1, 2); s(27, 1, 2); s(28, 1, 1); s(29, 1, 1);
        s(54, 2, 2); s(55, 2, 2); s(56, 2, 1); s(57, 2, 2);
        s(80, 3, 2); s(81, 3, 3); s(82, 3, 2); s(83, 3, 3); s(84, 3, 2);

        e(85, 1, "HUNT",            "Grants 1 PP per Hunter.");
        e(86, 1, "SUSTENANCE",      "Penalty: 1 PP per member without food.");
        e(87, 1, "SHAMANIC RITUAL", "Win: +5 PP. Lose: -3 PP.");
        e(88, 1, "CAVE PAINTINGS",  "Min. 1 Artist. Penalty: 2 PP. Reward: 1 PP per Artist.");
        e(89, 2, "HUNT",            "Grants 2 PP per Hunter.");
        e(90, 2, "SUSTENANCE",      "Penalty: 2 PP per member without food.");
        e(91, 2, "SHAMANIC RITUAL", "Win: +10 PP. Lose: -5 PP.");
        e(92, 2, "CAVE PAINTINGS",  "Min. 2 Artists. Penalty: 2 PP. Reward: 2 PP per Artist.");
        e(93, 3, "HUNT",            "Grants 3 PP per Hunter.");
        e(94, 3, "CAVE PAINTINGS",  "Min. 3 Artists. Penalty: 2 PP. Reward: 3 PP per Artist.");
        e(95, 0, "SUSTENANCE",      "Penalty: 3 PP per member without food.");
        e(96, 0, "SHAMANIC RITUAL", "Win: +15 PP. Lose: -7 PP.");

        bd(97,  1, 4,  3, "Earn 5 food each time you complete a new set of characters.");
        bd(98,  1, 4,  4, "Each Gatherer gives 1 food discount during Sustenance events.");
        bd(99,  1, 5,  3, "Each Artist gives 1 food discount during Sustenance events.");
        bd(100, 1, 5,  2, "Immunity to PP loss during Shamanic Ritual events.");
        bd(101, 1, 3,  3, "Earn 1 extra food when placing Totem on a food space.");
        bd(102, 1, 4,  3, "Earn 3 food each time you complete a new pair of Inventors.");
        bd(103, 2, 7,  0, "Double PP gained if you win a Shamanic Ritual event.");
        bd(104, 2, 6,  4, "Adds 3 temporary Shaman stars during Shamanic Ritual events.");
        bd(105, 2, 7,  4, "Each Inventor gives 1 food discount during Sustenance events.");
        bd(106, 2, 7,  2, "During Hunt events, earn 1 extra food and 1 extra PP per Hunter.");
        bd(107, 2, 6,  4, "At end of game, double the PP provided by your Builders.");
        bd(108, 2, 5,  6, "Earn 1 food per Artist during Cave Painting events.");
        bd(109, 2, 5,  6, "Earn 6 PP for each complete set of characters at end of game.");
        bd(110, 3, 8,  8, "Earn 3 PP for each Hunter at end of game.");
        bd(111, 3, 7,  6, "Earn 4 PP for each Gatherer at end of game.");
        bd(112, 3, 7,  4, "Earn 4 PP for each Shaman at end of game.");
        bd(113, 3, 6,  3, "Earn 4 PP for each Builder at end of game.");
        bd(114, 3, 7,  4, "Earn 4 PP for each Artist at end of game.");
        bd(115, 3, 6,  6, "Earn 2 PP for each Inventor at end of game.");
        bd(116, 3, 9,  3, "Draw an extra card at the end of each round.");
        bd(117, 3, 10, 0, "Earn 25 PP at the end of the game.");
    }

    public static String getCardType(int id) {
        CardData c = cards.get(id);
        return c != null ? c.type : "UNKNOWN";
    }

    public static String getCardName(int id) {
        CardData c = cards.get(id);
        if (c == null) return "Unknown Card";
        if ("EVENT".equals(c.type)) return c.detail.split("\n")[0];
        return c.type;
    }

    public static String getCardEra(int id) {
        CardData c = cards.get(id);
        if (c == null) return "";
        return c.era == 0 ? "FINAL EVENT" : "Era " + c.era;
    }

    public static int getBuildingCost(int id) {
        CardData c = cards.get(id);
        return c != null ? c.buildingCost : 0;
    }

    public static int getBuildingPP(int id) {
        CardData c = cards.get(id);
        return c != null ? c.buildingPP : 0;
    }

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