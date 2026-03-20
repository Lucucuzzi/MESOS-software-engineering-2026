package it.polimi.ingsw.am46.model;

public class Space {
    private final int pos;
    private final int food;
    private final int pp;
    private boolean occupied;

    public Space(int pos, int food, int pp) {
        this.pos = pos;
        this.food = food;
        this.pp = pp;
        this.occupied = false;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public int getPP() {
        return pp;
    }

    public int getFood() {
        return food;
    }

    public int getPos() {
        return pos;
    }
}
