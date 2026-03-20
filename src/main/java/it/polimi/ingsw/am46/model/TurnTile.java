package it.polimi.ingsw.am46.model;

import java.util.HashMap;
import java.util.Map;

public class TurnTile {
    private Map <Space,Player> totems;

    public TurnTile() {
        this.totems = new HashMap<>();
    }

    public void randomlyPlaceTotem(){}

    public Player takeTotem(){
        return null;
    }

    public void pushTotem(){}

    public void applyTTEffect(Space space){}

}
