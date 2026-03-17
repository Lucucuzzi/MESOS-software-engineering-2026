package it.polimi.ingsw.am46.model.cards.characterCards;

import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class Gatherer extends CharacterCard {


    public Gatherer(int id, int era, int cost, int minPlayers) {
        super(id, era, cost, SubType.GATHERER, minPlayers == 0 ? 2 : minPlayers);
    }



    @Override
    public SubType getSubType(){
        return SubType.GATHERER;
    }

}
