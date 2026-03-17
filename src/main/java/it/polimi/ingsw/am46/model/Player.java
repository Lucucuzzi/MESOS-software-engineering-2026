package it.polimi.ingsw.am46.model;

import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.model.cards.characterCards.CharacterCard;

import java.util.ArrayList;

public class Player {
    private String nickname;
    private Color color;
    private int food;
    private int pp;
    private ArrayList<BuildingCard> buildings;
    private ArrayList<CharacterCard> characters;

    public Player(String nickname){
        this.nickname = nickname; //ci sara metodo per settare il nome o direttamente nel costruttore?
        this.color = null;  //quando il game ti fara scegliere il color tra gli availableColors
        this.food = 0;           // i primi foods vengono assegnati con la orderTile
        this.pp = 0;             // si parte da 0 PP
        this.buildings = new ArrayList<>();   // inizialmente senza builiding
        this.characters = new ArrayList<>();  // tribù inizialmente vuota
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
        return buildings;
    }
    public ArrayList<CharacterCard> getCharacters() {
        return characters;
    }

    public void modifyFood(int food){
        this.food = this.food + food;
    }
    public void modifyPP(int pp){
        this.pp = this.pp + pp;
    }
    public void addCard(Card card){
        //logica da implementare, possibilmente fare due addCard
        // diverse per addBuildingCard e addCharacterCard.

    }
    public void confirm(){
        //logica da implementare;
    }

}
