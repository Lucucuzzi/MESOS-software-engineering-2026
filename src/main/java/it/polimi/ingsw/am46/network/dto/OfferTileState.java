package it.polimi.ingsw.am46.network.dto;

import it.polimi.ingsw.am46.model.OfferTile;

import java.io.Serializable;

public class OfferTileState implements Serializable {
    private final char letter;
    private final boolean occupied;
    private final String totemOwnerNickname;

    public OfferTileState(OfferTile tile) {
        this.letter = tile.getLetter();
        this.occupied = tile.isOccupied();
        this.totemOwnerNickname = tile.isOccupied()? tile.getTotem().getNickname() : null;
    }
    public char getLetter() { return letter; }
    public boolean isOccupied() { return occupied; }
    public String getTotemOwnerNickname() {
        return totemOwnerNickname; }
}




