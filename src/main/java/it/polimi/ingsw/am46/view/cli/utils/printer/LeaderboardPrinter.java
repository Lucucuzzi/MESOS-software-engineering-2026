package it.polimi.ingsw.am46.view.cli.utils.printer;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardPrinter {
    public static List<String> getLeaderboardLines(List<LeaderboardEntry> leaderboard, int numPlayers, int playerRank) {
        List<String> lines = new ArrayList<>();
        lines.add("\n" + "=".repeat(80));
        lines.add(" ".repeat(25) + "🏆 GLOBAL RANK 🏆");
        lines.add(" ".repeat(20) + String.format("Matches with %d players", numPlayers));
        lines.add("=".repeat(80));

        if (leaderboard == null || leaderboard.isEmpty()) {
            lines.add("Leaderboard empty.");
            lines.add("=".repeat(80) + "\n");
            return lines;
        }

        lines.add(String.format("%-6s | %-25s | %-12s", "Pos", "Nickname", "Victories"));
        lines.add("-".repeat(80));

        int rank = 1;
        for (LeaderboardEntry entry : leaderboard) {
            String medal = getMedal(rank);
            lines.add(String.format("%s %2d | %-25s | %12d",
                    medal, rank, entry.getNickname(), entry.getTotalWins()));
            rank++;
        }

        lines.add("=".repeat(80));
        if (playerRank > 0) {
            lines.add("🎯 Your position: #" + playerRank);
        }
        lines.add("=".repeat(80) + "\n");

        return lines;
    }

    private static String getMedal(int position) {
        return switch (position) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "  ";
        };
    }
}