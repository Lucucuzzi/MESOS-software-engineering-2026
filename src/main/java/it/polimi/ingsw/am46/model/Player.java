package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.Item;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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


    public Player(String nickname){
        this.nickname = nickname;
        this.color = null; //quando il game ti farà scegliere il color tra gli availableColors usi setColor
        this.food = 0; // i primi foods vengono assegnati con la orderTile
        this.pp = 0; // si parte da 0 PP
        this.buildings = new ArrayList<>(); // inizialmente senza builidings
        this.characters = new ArrayList<>(); // tribù inizialmente vuota
    }

    public String getNickname() {
        return this.nickname;
    }
    public Color getColor() {
        return this.color;
    }
    public int getFood() {
        return this.food;
    }
    public int getPP() {
        return this.pp;
    }
    public ArrayList<BuildingCard> getBuildings() {
        return this.buildings;
    }
    public ArrayList<CharacterCard> getCharacters() {
        return this.characters;
    }
    public boolean hasShamanImmunity() { return this.shamanImmunity; }
    public boolean hasShamanDoublePP() { return this.shamanDoublePP; }
    public boolean canTakeExtraCard() { return this.canTakeExtraCard; }
    public int getSustenanceDiscount() { return this.sustenanceDiscount; }
    public int getExtraShamanIcons() { return this.extraShamanIcons; }

    public void setColor(Color color){
        this.color = color;
    }
    public void modifyFood(int food){
        this.food = this.food + food;
    }
    public void modifyPP(int pp){
        this.pp = this.pp + pp;
    }

    public void addCard(BuildingCard card){
        this.buildings.add(card);
    }
    public void addCard(CharacterCard card){
        this.characters.add(card);
    }


    //FLAG SETTERS — called by building effects

    // EFFECT5 — shaman immunity
    public void setShamanImmunity(boolean value) {
        this.shamanImmunity = value;
    }

    // EFFECT7 — double PP if shaman majority
    public void setShamanDoublePP(boolean value) {
        this.shamanDoublePP = value;
    }

    // EFFECT11 — extra card before end round
    public void setCanTakeExtraCard(boolean value) {
        this.canTakeExtraCard = value;
    }

    // EFFECT2 — sustenance discount per character type
    public void addSustenanceDiscount(int amount) {
        this.sustenanceDiscount += amount;
    }

    // EFFECT6 — extra shaman icons
    public void addExtraShamanIcons() {
        this.extraShamanIcons += 3;
    }

    // RESET FLAGS — called by Game after each event
    //these are needed, if not present for example, if we have a building that
    //does give 3 extra shaman icons, after 2 events of that type we will have 6 extra
    //shaman icons, even if we have just one building of this type, so since the game
    //trigger ONEVENT every time we need to reset the value

    // called by Game after ShamanRitual is resolved
    public void resetShamanFlags() {
        this.shamanImmunity = false;
        this.shamanDoublePP = false;
        this.extraShamanIcons = 0;
    }

    // called by Game after Sustenance is resolved
    public void resetSustenanceDiscount() {
        this.sustenanceDiscount = 0;
    }

    // called by Game after extra card is handled
    public void resetExtraCard() {
        this.canTakeExtraCard = false;
    }
    // ========== COUNTING METHODS — used by building effects ==========

    // used by EFFECT4, EFFECT8, EFFECT2, EFFECT14
    public int countCharactersByType(SubType type) {
        int count = 0;
        for (CharacterCard c : characters) {
            if (c.getSubType() == type) count++;
        }
        return count;
    }

    // used by EFFECT9, EFFECT13
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

    // used by EFFECT3 — Game calls this before and after addCard
    public int countInventorPairs() {
        Map<Item, Integer> iconCount = new HashMap<>();
        for (CharacterCard c : characters) {
            if (c.getSubType() == SubType.INVENTOR) {
                //iconCount.put(c.getItem(), iconCount.getOrDefault(c.getItem(), 0) + 1);
            }
        }
        int pairs = 0;
        for (int count : iconCount.values()) {
            pairs += count / 2;
        }
        return pairs;
    }

    // used by EFFECT12
    public int calculateBuilderPP() {
        int total = 0;
        for (CharacterCard c : characters) {
            if (c.getSubType() == SubType.BUILDER) {
                //total += c.getPP();
            }
        }
        return total;
    }

    // used by ShamanRitual event
    public int countShamanIcons() {
        int total = 0;
        for (CharacterCard c : characters) {
            if (c.getSubType() == SubType.SHAMAN) {
                //total += c.getStars();
            }
        }
        total += this.extraShamanIcons;
        return total;
    }






}
