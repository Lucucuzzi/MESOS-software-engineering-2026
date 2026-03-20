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
        this.nickname = nickname;
        this.color = null; //quando il game ti fara scegliere il color tra gli availableColors usi setColor
        this.food = 0; // i primi foods vengono assegnati con la orderTile
        this.pp = 0; // si parte da 0 PP
        this.buildings = new ArrayList<>(); // inizialmente senza builiding
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
        this.food = this.food + food;
    }
    public void modifyPP(int pp){
        this.pp = this.pp + pp;
    }

    // Le possibili soluzioni (si potrebbe anche lasciarle tutte quante in realtà funziona bene lo stesso)
    public void addCard(Card card) {
        if (card == null) return;
        switch (card.getType()) {
            case BUILDING:
                addCard((BuildingCard) card);
                break;
            case CHARACTER:
                addCard((CharacterCard) card);
                break;
            case EVENT:
                break;
        }
    }

    public void addCard(BuildingCard card){
        this.buildings.add(card);
    }

    public void addCard(CharacterCard card){
        this.characters.add(card);
    }





}
