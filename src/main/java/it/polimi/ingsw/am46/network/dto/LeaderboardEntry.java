package it.polimi.ingsw.am46.network.dto;

import java.io.Serializable;

/**
 * The type Leaderboard entry.
 */
public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final int totalWins;

    /**
     * Instantiates a new Leaderboard entry.
     *
     * @param nickname  the nickname
     * @param totalWins the total wins
     */
    public LeaderboardEntry(String nickname, int totalWins) {
        this.nickname = nickname;
        this.totalWins = totalWins;
    }

    /**
     * Gets nickname.
     *
     * @return the nickname
     */
    public String getNickname() { return nickname; }

    /**
     * Gets total wins.
     *
     * @return the total wins
     */
    public int getTotalWins() { return totalWins; }

    @Override
    public String toString() {
        return String.format("LeaderboardEntry{nickname='%s', totalWins=%d}", nickname, totalWins);
    }
}