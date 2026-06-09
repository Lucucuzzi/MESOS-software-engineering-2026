package it.polimi.ingsw.am46.server.model;

import it.polimi.ingsw.am46.server.model.cards.Card;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.server.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.server.model.cards.enums.Item;
import it.polimi.ingsw.am46.server.model.cards.enums.SubType;

import java.util.*;

/**
 * The type Player.
 */
public class Player {
    private final String nickname;
    private Color color;
    private int food;
    private int pp;
    private ArrayList<BuildingCard> buildings;
    private ArrayList<CharacterCard> characters;

    //flags for building effects
    private boolean shamanImmunity = false;
    private boolean shamanDoublePP = false;
    private boolean canTakeExtraCard = false;
    private int sustenanceDiscount = 0;
    private int extraShamanIcons = 0;
    private int newlyFormedInventorPairs = 0;
    private int newlyFormedSets = 0;

    // Campo: false = online, true = disconnesso
    private boolean disconnected = false;


    /**
     * Instantiates a new Player.
     *
     * @param nickname the nickname
     */
    public Player(String nickname){
        this.nickname = nickname;
        this.color = null; // when the game lets the player choose a color from availableColors, use setColor
        this.food = 0; // the initial food is assigned through the order tile
        this.pp = 0; // players start with 0 PP
        this.buildings = new ArrayList<>(); // initially no buildings
        this.characters = new ArrayList<>(); // the tribe starts empty
    }

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Gets food.
     *
     * @return the food
     */
    public int getFood() {
        return this.food;
    }

    /**
     * Gets pp.
     *
     * @return the pp
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Gets buildings.
     *
     * @return the buildings
     */
    public List<BuildingCard> getBuildings() {
        return new ArrayList<> (this.buildings);
    }

    /**
     * Gets characters.
     *
     * @return the characters
     */
    public List<CharacterCard> getCharacters() {
        return new ArrayList<>(this.characters);
    }

    /**
     * Has shaman immunity boolean.
     *
     * @return the boolean
     */
    public boolean hasShamanImmunity() { return this.shamanImmunity; }

    /**
     * Has shaman double pp boolean.
     *
     * @return the boolean
     */
    public boolean hasShamanDoublePP() { return this.shamanDoublePP; }

    /**
     * Can take extra card boolean.
     *
     * @return the boolean
     */
    public boolean canTakeExtraCard() { return this.canTakeExtraCard; }

    /**
     * Gets sustenance discount.
     *
     * @return the sustenance discount
     */
    public int getSustenanceDiscount() { return this.sustenanceDiscount; }

    /**
     * Gets extra shaman icons.
     *
     * @return the extra shaman icons
     */
    public int getExtraShamanIcons() { return this.extraShamanIcons; }

    /**
     * Get cards list .
     *
     * @return the list
     */
    public List <Card> getCards(){
        List <Card> cards = new ArrayList<>();
        cards.addAll(buildings);
        cards.addAll(characters);
        return cards;
    }

    /**
     * Is disconnected boolean.
     *
     * @return the boolean
     */
    public boolean isDisconnected() {
        return disconnected;
    }

    /**
     * Set color.
     *
     * @param color the color
     */
    public void setColor(Color color){
        this.color = color;
    }

    /**
     * Sets disconnected.
     *
     * @param disconnected the disconnected
     */
    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    /**
     * Modify food.
     *
     * @param food the food
     */
    public void modifyFood(int food){
        this.food = this.food + food;
    }

    /**
     * Modify pp.
     *
     * @param pp the pp
     */
    public void modifyPP(int pp){
        this.pp = this.pp + pp;
    }

    /**
     * Add card.
     *
     * @param card the card
     */
    public void addCard(BuildingCard card){
        this.buildings.add(card);
    }

    /**
     * Add card.
     *
     * @param card the card
     */
    public void addCard(CharacterCard card){
        this.characters.add(card);
        card.applyEffect(this);
    }


    //FLAG SETTERS — called by building effects

    /**
     * Sets shaman immunity.
     *
     * @param value the value
     */
    public void setShamanImmunity(boolean value) {
        this.shamanImmunity = value;
    }

    /**
     * Sets shaman double pp.
     *
     * @param value the value
     */
    public void setShamanDoublePP(boolean value) {
        this.shamanDoublePP = value;
    }

    /**
     * Sets can take extra card.
     *
     * @param value the value
     */
    public void setCanTakeExtraCard(boolean value) {
        this.canTakeExtraCard = value;
    }

    /**
     * Add sustenance discount.
     *
     * @param amount the amount
     */
    public void addSustenanceDiscount(int amount) {
        this.sustenanceDiscount += amount;
    }

    /**
     * Add extra shaman icons.
     */
    public void addExtraShamanIcons() {
        this.extraShamanIcons += 3;
    }

    /**
     * Sets newly formed inventor pairs.
     *
     * @param pairs the pairs
     */
// for inventor pairs
    public void setNewlyFormedInventorPairs(int pairs) {
        this.newlyFormedInventorPairs = pairs;
    }

    /**
     * Gets newly formed inventor pairs.
     *
     * @return the newly formed inventor pairs
     */
    public int getNewlyFormedInventorPairs() {
        return this.newlyFormedInventorPairs;
    }

    /**
     * Sets newly formed sets.
     *
     * @param sets the sets
     */
// for complete sets, used to check whether the set is new
    public void setNewlyFormedSets(int sets) {
        this.newlyFormedSets = sets;
    }

    /**
     * Gets newly formed sets.
     *
     * @return the newly formed sets
     */
    public int getNewlyFormedSets() {
        return this.newlyFormedSets;
    }



    // RESET FLAGS — called by Game after each event


    /**
     * Reset shaman flags.
     */
// called by Game after ShamanRitual is resolved
    public void resetShamanFlags() {
        this.shamanImmunity = false;
        this.shamanDoublePP = false;
        this.extraShamanIcons = 0;
    }

    /**
     * Reset sustenance discount.
     */
// called by Game after Sustenance is resolved
    public void resetSustenanceDiscount() {
        this.sustenanceDiscount = 0;
    }

    /**
     * Reset extra card.
     */
// called by Game after extra card is handled
    public void resetExtraCard() {
        this.canTakeExtraCard = false;
    }

    /**
     * Reset newly formed inventor pairs.
     */
    public void resetNewlyFormedInventorPairs() {
        this.newlyFormedInventorPairs = 0;
    }

    /**
     * Reset newly formed sets.
     */
    public void resetNewlyFormedSets() {
        this.newlyFormedSets = 0;
    }
    // ========== COUNTING METHODS — used by building effects ==========

    /**
     * Count characters by type int.
     *
     * @param type the type
     * @return the int
     */
    public int countCharactersByType(SubType type) {
        int count = 0;
        for (CharacterCard c : characters) {
            if (c.getSubType() == type) count++;
        }
        return count;
    }

    /**
     * Count complete sets int.
     *
     * @return the int
     */
    public int countCompleteSets() {
        int hunters   = countCharactersByType(SubType.HUNTER);
        int shamans   = countCharactersByType(SubType.SHAMAN);
        int artists   = countCharactersByType(SubType.ARTIST);
        int builders  = countCharactersByType(SubType.BUILDER);
        int inventors = countCharactersByType(SubType.INVENTOR);
        int gatherers = countCharactersByType(SubType.GATHERER);

        return Math.min(hunters,
                Math.min(shamans,
                        Math.min(artists,
                                Math.min(builders,
                                        Math.min(inventors, gatherers)))));
    }

    /**
     * Count inventor pairs int.
     *
     * @return the int
     */
    public int countInventorPairs() {
        Map<Item, Integer> iconCount = new HashMap<>();
        for (CharacterCard c : characters) {
            c.getItem().ifPresent(item ->
                    iconCount.put(item, iconCount.getOrDefault(item, 0) + 1)
            );
        }
        int pairs = 0;
        for (int count : iconCount.values()) {
            pairs += count / 2;
        }
        return pairs;
    }

    /**
     * Calculate builder pp int.
     *
     * @return the int
     */
    public int calculateBuilderPP() {
        int total = 0;
        for (CharacterCard c : characters) {
            total+=c.getPp();
        }
        return total;
    }

    /**
     * Calculate artist bonus int.
     *
     * @return the int
     */
    public int calculateArtistBonus() {
        int numArtists = countCharactersByType(SubType.ARTIST);
        return (numArtists / 2) * 10;
    }

    /**
     * Calculate inventor bonus int.
     *
     * @return the int
     */
    public int calculateInventorBonus() {
        Set<Item> uniqueItems = new HashSet<>();
        for (CharacterCard c : characters) {
            if (c.getSubType() == SubType.INVENTOR) {
                c.getItem().ifPresent(uniqueItems::add);
            }
        }
        int numInventors = countCharactersByType(SubType.INVENTOR);
        return uniqueItems.size() * numInventors;
    }

    /**
     * Count shaman icons int.
     *
     * @return the int
     */
    public int countShamanIcons() {
        int total = 0;
        for (CharacterCard c : characters) {
            total+=c.getStars();
        }
        total += this.extraShamanIcons;
        return total;
    }

    /**
     * Sets food.
     *
     * @param food the food
     */
    public void setFood(int food) {
        this.food = food;
    }

    /**
     * Sets pp.
     *
     * @param pp the pp
     */
    public void setPp(int pp) {
        this.pp = pp;
    }

}
