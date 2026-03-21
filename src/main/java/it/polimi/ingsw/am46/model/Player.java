package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

import java.util.ArrayList;

public class Player {
    private final String nickname;
    private Color color;
    private int food;
    private int pp;
    private ArrayList<BuildingCard> buildings;
    private ArrayList<CharacterCard> characters;
    private int temporarySustenanceDiscount = 0;
    private int temporaryShamanIcons = 0;
    private boolean shamanImmunity = false;
    private boolean shamanDoublePP =  false;


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

    public void setColor(Color color){
        this.color = color;
    }
    public void modifyFood(int food){
        //il cibo non va mai sotto zero
        this.food = Math.max(0, this.food + food);
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

    //aggiunto questo metodo per comodità, conta quanti personaggi di un certo tipo hai
    public int countCharactersByType(SubType type) {
        return (int) this.characters.stream()
                .filter(card -> card.getSubType() == type)
                .count();
    }

    public int getTemporarySustenanceDiscount() {
        return temporarySustenanceDiscount;
    }

    public int getTemporaryShamanIcons() {
        return temporaryShamanIcons;
    }
    public boolean getShamanImmunity() {
        return shamanImmunity;
    }
    public boolean getShamanDoublePP() {
        return shamanDoublePP;
    }

    public void setTemporarySustenanceDiscount(int temporarySustenanceDiscount) {
        this.temporarySustenanceDiscount = temporarySustenanceDiscount;
    }

    public void setTemporaryShamanIcons(int temporaryShamanIcons) {
        this.temporaryShamanIcons = temporaryShamanIcons;
    }

    public void setShamanImmunity(boolean shamanImmunity) {
        this.shamanImmunity = shamanImmunity;
    }
    public void setShamanDoublePP(boolean shamanDoublePP) {
        this.shamanDoublePP = shamanDoublePP;
    }

}
