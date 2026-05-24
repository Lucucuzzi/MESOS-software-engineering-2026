package it.polimi.ingsw.am46.network.dto;

import it.polimi.ingsw.am46.model.Color;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.cards.Card;

import java.io.Serializable;
import java.util.List;

public class PlayerState implements Serializable {
    private final String nickname;
    private final int food;
    private final Color color;
    private final int pp;
    private final List<Integer> characterCardIds;
    private final List<Integer> buildingCardIds;
    private final List<Integer> cardIds;
    private final boolean isActive;
    private boolean disconnected;

    public PlayerState(Player player) {
        this.nickname = player.getNickname();
        this.food = player.getFood();
        this.color = player.getColor();
        this.pp = player.getPP();
        this.disconnected = player.isDisconnected();
        this.isActive = false; // impostato dal costruttore di GameState
        this.characterCardIds = player.getCharacters()
                .stream().map(Card::getId).toList();
        this.buildingCardIds = player.getBuildings()
                .stream().map(Card::getId).toList();
        this.cardIds = player.getCards().stream().map(Card ::getId).toList();
    }
    public Color getColor() { return color; }
    public String getNickname() { return nickname; }
    public int getFood() { return food; }
    public int getPP() { return pp; }
    public List<Integer> getCharacterCardIds() {
        return characterCardIds; }
    public List<Integer> getBuildingCardIds() {
        return buildingCardIds; }
    public boolean isActive() { return isActive; }
    public List<Integer> getCardIds() {
        return cardIds;
    }
    public boolean isDisconnected() { return disconnected; }
}
