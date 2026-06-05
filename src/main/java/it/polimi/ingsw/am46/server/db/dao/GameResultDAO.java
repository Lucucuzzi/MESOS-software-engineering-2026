package it.polimi.ingsw.am46.server.db.dao;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import java.util.List;

public interface GameResultDAO {
    void saveGameResults(int numPlayers, List<String> nicknames, List<Integer> scores, List<Integer> positions);
    List<LeaderboardEntry> getLeaderboard(int numPlayers);
    int getPlayerPosition(String nickname, int numPlayers);
}
