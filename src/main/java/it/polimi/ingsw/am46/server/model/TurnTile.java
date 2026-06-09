package it.polimi.ingsw.am46.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type Turn tile.
 */
public class TurnTile {
    private List<Space> spaces;

    /**
     * Instantiates a new Turn tile.
     *
     * @param initialSpaces the initial spaces
     */
// The constructor receives the configuration of the specific spaces created from JSON
    public TurnTile(List<Space> initialSpaces) {
        this.spaces = new ArrayList<>(initialSpaces);
    }


    /**
     * Push totem.
     *
     * @param player the player
     */
// Adds the totem (Player) to the first available position
    public void pushTotem(Player player) {
        if (getSpaceOfPlayer(player) != null) {
            throw new IllegalArgumentException("Player is already on the Turn Tile!");
        }

        for (Space s : spaces) {
            if (!s.isOccupied()) {
                s.setPlayer(player);
                return;
            }
        }

        throw new IllegalStateException("Turn Tile is full, cannot add more players!");
    }

    /**
     * Take totem player.
     *
     * @param position the position
     * @return the player
     */
// Takes the totem from the given position and returns the Player
    public Player takeTotem(int position) {
        for (Space s : spaces) {
            if (s.getPos() == position) {
                Player p = s.getPlayer();
                s.setPlayer(null);
                return p;
            }
        }
        return null;
    }

    /**
     * Apply tt effect.
     *
     * @param space the space
     */
// Applies the space effect to the player occupying it
    public void applyTTEffect(Space space) {
        Player p = space.getPlayer();

        if(p==null){
            return;
        }

        if (space.getFood() < 0) {
            if (p.getFood() >= Math.abs(space.getFood())) {
                p.modifyFood(space.getFood());
            } else {
                p.modifyPP(space.getPP()); // If player doesn't have food, he has to pay 2PP
            }
        } else {
            p.modifyFood(space.getFood()); // Bonus
        }
    }

    /**
     * Gets turn order.
     *
     * @return the turn order
     */
// Returns the list of players in the correct turn order
    public List<Player> getTurnOrder() {
        return spaces.stream()
                .map(Space::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Randomly place totems.
     *
     * @param players the players
     */
// Takes the full player list and places their totems randomly
    public void randomlyPlaceTotems(List<Player> players) {
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            Player p = shuffledPlayers.get(i);
            this.pushTotem(p);
        }
    }

    /**
     * Gets space of player.
     *
     * @param player the player
     * @return the space of player
     */
// Returns the space currently occupied by the given Player
    public Space getSpaceOfPlayer(Player player) {
        for (Space s : spaces) {
            if (s.getPlayer() == player) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets spaces.
     *
     * @return the spaces
     */
    public List<Space> getSpaces() {
        return spaces;
    }

}
