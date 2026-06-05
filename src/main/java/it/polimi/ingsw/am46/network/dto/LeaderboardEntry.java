package it.polimi.ingsw.am46.network.dto;

import java.io.Serializable;

public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final int totalWins;

    public LeaderboardEntry(String nickname, int totalWins) {
        this.nickname = nickname;
        this.totalWins = totalWins;
    }

    public String getNickname() { return nickname; }
    public int getTotalWins()   { return totalWins; }

    @Override
    public String toString() {
        return String.format("LeaderboardEntry{nickname='%s', totalWins=%d}", nickname, totalWins);
    }
}
