package it.polimi.ingsw.am46.server.model;

/**
 * The type Offer tile.
 */
public class OfferTile {
    private final char letter;
    private final int number;
    private Player totem;
    private final int numCardFromDown;
    private final int numCardFromAbove;
    private final int food;
    private boolean occupied;

    /**
     * Instantiates a new Offer tile.
     *
     * @param letter           the letter
     * @param number           the number
     * @param numCardFromAbove the num card from above
     * @param numCardFromBelow the num card from below
     * @param food             the food
     */
    public OfferTile(char letter, int number, int numCardFromAbove, int numCardFromBelow, int food) {
        this.letter = letter;
        this.number = number;
        this.numCardFromAbove = numCardFromAbove;
        this.numCardFromDown = numCardFromBelow;
        this.food = food;
        this.occupied = false;
        this.totem = null;
    }

    /**
     * Is occupied boolean.
     *
     * @return the boolean
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Gets food.
     *
     * @return the food
     */
    public int getFood() {
        return food;
    }

    /**
     * Gets num card from above.
     *
     * @return the num card from above
     */
    public int getNumCardFromAbove() {
        return numCardFromAbove;
    }

    /**
     * Gets num card from down.
     *
     * @return the num card from down
     */
    public int getNumCardFromDown() {
        return numCardFromDown;
    }

    /**
     * Gets totem.
     *
     * @return the totem
     */
    public Player getTotem() {
        return totem;
    }

    /**
     * Sets totem.
     *
     * @param totem the totem
     */
    public void setTotem(Player totem) {
        this.totem = totem;
    }

    /**
     * Sets occupied.
     *
     * @param occupied the occupied
     */
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    /**
     * Place totem.
     *
     * @param totem the totem
     */
    public void placeTotem(Player totem){
        if (!this.occupied && totem != null) {
            this.totem = totem;
            this.occupied = true;
        }
    }

    /**
     * Remove totem.
     */
    public void removeTotem(){
        if (this.occupied) {
            this.totem = null;
            this.occupied = false;
        }
    }

    /**
     * Gets letter.
     *
     * @return the letter
     */
    public char getLetter() {
        return letter;
    }
}
