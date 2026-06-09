package it.polimi.ingsw.am46.server.db.dao;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import java.util.List;

/**
 * The interface Game result dao.
 */
public interface GameResultDAO {
    /**
     * Save game results.
     *
     * @param numPlayers the num players
     * @param nicknames  the nicknames
     * @param scores     the scores
     * @param positions  the positions
     */
    void saveGameResults(int numPlayers, List<String> nicknames, List<Integer> scores, List<Integer> positions);

    /**
     * Gets leaderboard.
     *
     * @param numPlayers the num players
     * @return the leaderboard
     */
    List<LeaderboardEntry> getLeaderboard(int numPlayers);

    /**
     * Gets player position.
     *
     * @param nickname   the nickname
     * @param numPlayers the num players
     * @return the player position
     */
    int getPlayerPosition(String nickname, int numPlayers);
}
