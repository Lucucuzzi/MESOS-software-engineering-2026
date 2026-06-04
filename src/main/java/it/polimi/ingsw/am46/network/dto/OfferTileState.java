package it.polimi.ingsw.am46.network.dto;

import it.polimi.ingsw.am46.server.model.OfferTile;

import java.io.Serializable;

public class OfferTileState implements Serializable {
    private final char letter;
    private final boolean occupied;
    private final String totemOwnerNickname;
    public int bottomRow;
    public int topRow;
    public int food;

    public OfferTileState(OfferTile tile) {
        this.letter = tile.getLetter();
        this.occupied = tile.isOccupied();
        this.totemOwnerNickname = tile.isOccupied()? tile.getTotem().getNickname() : null;
        this.bottomRow = tile.getNumCardFromDown();
        this.topRow = tile.getNumCardFromAbove();
        this.food = tile.getFood();
    }
    public char getLetter() { return letter; }
    public boolean isOccupied() { return occupied; }
    public String getTotemOwnerNickname() {
        return totemOwnerNickname; }
    public int getBottomRow() { return bottomRow; }
    public int getTopRow() { return topRow; }
    public int getFood() { return food; }

}




