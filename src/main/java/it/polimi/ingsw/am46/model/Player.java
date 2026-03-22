package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;

import java.util.ArrayList;

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

    // returns the number of characters of the given type in the player's tribe
    public int countCharactersByType() {
        // iterate characters list
        // count how many have subType == type
        // return count
        return 1;
    }

    // returns the number of complete sets (one of each of the 6 types)
    public int countCompleteSets() {
        // call countCharactersByType for each of the 6 subtypes
        // return the minimum value among all 6 counts
        // (the minimum is the bottleneck that limits complete sets)
        return 1;
    }

    // returns the number of inventor pairs with the same icon
// called by Game BEFORE and AFTER addCard to detect new pairs
    public int countInventorPairs() {
        // iterate characters list filtering by SUBTYPE.INVENTOR
        // group inventors by their icon
        // for each icon group, pairs = count / 2
        // return total pairs
        return 1;
    }

    // returns total shaman icons including extra icons from buildings
    public int countShamanIcons() {
        // iterate characters list filtering by SUBTYPE.SHAMAN
        // sum all stars values
        // add extraShamanIcons (from EFFECT6 buildings)
        // return total
        return 1;
    }

    // returns the base PP of all Builder cards (before any doubling)
    public int calculateBuilderPP() {
        // iterate characters list filtering by SUBTYPE.BUILDER
        // sum all pp values from each Builder
        // return total
        return 1;
    }

    //ho messo i return 1 per evitare i warnings, la logica è da implementare.






}
