package it.polimi.ingsw.am46.model.cards.eventCards;

import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.enums.SubType;

public class CavePaintings extends EventCard {
    private final int minArtistRequired;
    private final int ppPenalty;
    private final int ppRewardArtist;

    public CavePaintings(int id, int era, int cost, boolean finalEvent, int minArtistRequired, int ppPenalty, int ppRewardArtist) {
        super(id, era, cost, SubType.CAVEP, finalEvent);
        this.minArtistRequired = minArtistRequired;
        this.ppPenalty = ppPenalty;
        this.ppRewardArtist = ppRewardArtist;
    }

    @Override
    public void resolve(GameContext gameContext) {
        for (Player player : gameContext.getPlayers()) {
           int artists = player.countCharactersByType(SubType.ARTIST);
           if (artists < this.minArtistRequired) {
               player.modifyPP(-(this.ppPenalty));
           }else{
               player.modifyPP(artists * this.ppRewardArtist);
           }
        }
    }

    @Override
    public SubType getSubType() {
        return SubType.CAVEP;
    }
}
