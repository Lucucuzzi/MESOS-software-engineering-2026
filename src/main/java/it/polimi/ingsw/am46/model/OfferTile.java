package it.polimi.ingsw.am46.model;

import java.util.Optional;

public class OfferTile {
    private final char letter;
    private final int number;
    private Player totem;
    private final int numCardFromDown;
    private final int numCardFromAbove;
    private final int food;
    private boolean occupied;

    public OfferTile(char letter, int number, int numCardFromAbove, int numCardFromBelow, int food) {
        this.letter = letter;
        this.number = number;
        this.numCardFromAbove = numCardFromAbove;
        this.numCardFromDown = numCardFromBelow;
        this.food = food;
        this.occupied = false;
        this.totem = null;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public int getFood() {
        return food;
    }

    public int getNumCardFromAbove() {
        return numCardFromAbove;
    }

    public int getNumCardFromDown() {
        return numCardFromDown;
    }

    public Player getTotem() {
        return totem;
    }

    public void setTotem(Player totem) {
        this.totem = totem;
    }

    public int getNumber() {
        return number;
    }

    public char getLetter() {
        return letter;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void placeTotem(Player totem){
        if (!this.occupied && totem != null) {
            this.totem = totem;
            this.occupied = true;
        }
    }

    public void removeTotem(){
        if (this.occupied) {
            this.totem = null;
            this.occupied = false;
        }
    }
}
