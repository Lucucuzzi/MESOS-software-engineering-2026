package it.polimi.ingsw.am46.server.model;

public class Space {
    private Player totem;
    private final int pos;
    private final int food;
    private final int pp;
    private boolean occupied;

    public Space(int pos, int food, int pp) {
        this.pos = pos;
        this.food = food;
        this.pp = pp;
        this.occupied = false;
        this.totem = null;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public int getPP() { return pp; }

    public int getFood() { return food; }

    public int getPos() {
        return pos;
    }

    public Player getPlayer() { return totem; }

    public void setPlayer(Player player) {
        this.totem = player;
        // If player=null the space it remains unchanged
        this.occupied = (player != null);
    }
}
