package it.polimi.ingsw.am46.model;

import java.util.Optional;


//capire come fare con gli Optional come gestirli se servono oppure no
//se usiamo isOccupied c'è bisogno di avere un Optional?
public class OfferTile {
    private final char letter;
    private final Optional<Integer> number;
    private Player totem;
    private final int cardFromDown;
    private final int cardFromAbove;
    private final int food;
    private boolean occupied;

    public OfferTile(char letter, Optional<Integer> number, int cardFromDown, int cardFromAbove, int food) {
        this.letter = letter;
        this.number = number;
        this.cardFromDown = cardFromDown;
        this.cardFromAbove = cardFromAbove;
        this.food = food;
        this.occupied = false;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public int getFood() {
        return food;
    }

    public int getCardFromAbove() {
        return cardFromAbove;
    }

    public int getCardFromDown() {
        return cardFromDown;
    }

    public Player getTotem() {
        return totem;
    }

    public void setTotem(Player totem) {
        this.totem = totem;
    }

    public Optional<Integer> getNumber() {
        return number;
    }

    public char getLetter() {
        return letter;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void placeTotem(Color totem){
        //logica per piazzare i totem
    }

    public void removeTotem(){
        //logica per togliere il totem (se occupato)
    }
}
