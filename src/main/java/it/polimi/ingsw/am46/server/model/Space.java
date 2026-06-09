package it.polimi.ingsw.am46.server.model;

/**
 * The type Space.
 */
public class Space {
    private Player totem;
    private final int pos;
    private final int food;
    private final int pp;
    private boolean occupied;

    /**
     * Instantiates a new Space.
     *
     * @param pos  the pos
     * @param food the food
     * @param pp   the pp
     */
    public Space(int pos, int food, int pp) {
        this.pos = pos;
        this.food = food;
        this.pp = pp;
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
     * Gets pp.
     *
     * @return the pp
     */
    public int getPP() { return pp; }

    /**
     * Gets food.
     *
     * @return the food
     */
    public int getFood() { return food; }

    /**
     * Gets pos.
     *
     * @return the pos
     */
    public int getPos() {
        return pos;
    }

    /**
     * Gets player.
     *
     * @return the player
     */
    public Player getPlayer() { return totem; }

    /**
     * Sets player.
     *
     * @param player the player
     */
    public void setPlayer(Player player) {
        this.totem = player;
        // If player=null the space it remains unchanged
        this.occupied = (player != null);
    }
}
