package it.polimi.ingsw.am46.network.dto;

import it.polimi.ingsw.am46.server.model.OfferTile;

import java.io.Serializable;

/**
 * The type Offer tile state.
 */
public class OfferTileState implements Serializable {
    private final char letter;
    private final boolean occupied;
    private final String totemOwnerNickname;
    /**
     * The Bottom row.
     */
    public int bottomRow;
    /**
     * The Top row.
     */
    public int topRow;
    /**
     * The Food.
     */
    public int food;

    /**
     * Instantiates a new Offer tile state.
     *
     * @param tile the tile
     */
    public OfferTileState(OfferTile tile) {
        this.letter = tile.getLetter();
        this.occupied = tile.isOccupied();
        this.totemOwnerNickname = tile.isOccupied()? tile.getTotem().getNickname() : null;
        this.bottomRow = tile.getNumCardFromDown();
        this.topRow = tile.getNumCardFromAbove();
        this.food = tile.getFood();
    }

    /**
     * Gets letter.
     *
     * @return the letter
     */
    public char getLetter() { return letter; }

    /**
     * Is occupied boolean.
     *
     * @return the boolean
     */
    public boolean isOccupied() { return occupied; }

    /**
     * Gets totem owner nickname.
     *
     * @return the totem owner nickname
     */
    public String getTotemOwnerNickname() {
        return totemOwnerNickname; }

    /**
     * Gets bottom row.
     *
     * @return the bottom row
     */
    public int getBottomRow() { return bottomRow; }

    /**
     * Gets top row.
     *
     * @return the top row
     */
    public int getTopRow() { return topRow; }

    /**
     * Gets food.
     *
     * @return the food
     */
    public int getFood() { return food; }

}




