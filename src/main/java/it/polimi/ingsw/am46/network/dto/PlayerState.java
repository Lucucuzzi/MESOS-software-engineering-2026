package it.polimi.ingsw.am46.network.dto;

import it.polimi.ingsw.am46.server.model.Color;
import it.polimi.ingsw.am46.server.model.Player;
import it.polimi.ingsw.am46.server.model.cards.Card;

import java.io.Serializable;
import java.util.List;

/**
 * The type Player state.
 */
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

    /**
     * Instantiates a new Player state.
     *
     * @param player the player
     */
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

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() { return color; }

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() { return nickname; }

    /**
     * Gets food.
     *
     * @return the food
     */
    public int getFood() { return food; }

    /**
     * Gets pp.
     *
     * @return the pp
     */
    public int getPP() { return pp; }

    /**
     * Gets character card ids.
     *
     * @return the character card ids
     */
    public List<Integer> getCharacterCardIds() {
        return characterCardIds; }

    /**
     * Gets building card ids.
     *
     * @return the building card ids
     */
    public List<Integer> getBuildingCardIds() {
        return buildingCardIds; }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive() { return isActive; }

    /**
     * Gets card ids.
     *
     * @return the card ids
     */
    public List<Integer> getCardIds() {
        return cardIds;
    }

    /**
     * Is disconnected boolean.
     *
     * @return the boolean
     */
    public boolean isDisconnected() { return disconnected; }
}
