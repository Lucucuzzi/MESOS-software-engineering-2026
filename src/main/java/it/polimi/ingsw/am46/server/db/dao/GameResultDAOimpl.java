package it.polimi.ingsw.am46.server.db.dao;

import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.server.db.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameResultDAOimpl implements GameResultDAO {

    @Override
    public void saveGameResults(int numPlayers, List<String> nicknames, List<Integer> scores,  List<Integer> positions) {
        // Step 1: inserisci la partita e recupera l'id generato
        int matchId = saveMatch(numPlayers);
        if (matchId == -1) {
            System.err.println("DB error: failed to save match, skipping results.");
            return;
        }
        // Step 2: inserisci i risultati di tutti i giocatori in batch
        saveResults(matchId, nicknames, scores, positions);
    }

    private void saveResults(int matchId, List<String> nicknames, List<Integer> scores, List<Integer> positions) {
        String sql = "INSERT INTO MATCH_RESULTS (match_id, nickname, score, final_position) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (int i = 0; i < nicknames.size(); i++) {
                statement.setInt(1, matchId);
                statement.setString(2, nicknames.get(i));
                statement.setInt(3, scores.get(i));
                statement.setInt(4, positions.get(i));
                statement.addBatch(); //mette in coda la query da eseguire
            }
            int[] results = statement.executeBatch(); //esegue la query
            System.out.println("[DB] Righe inserite: " + results.length);

        } catch (SQLException e) {
            System.err.println("DB error saving results: " + e.getMessage());
        }
    }

    private int saveMatch(int numPlayers) {
        String sql = "INSERT INTO MATCHES (num_players) VALUES (?)";
        try (Connection conn = DataBaseConnection.getConnection();
             // Statement.RETURN_GENERATED_KEYS dice a JDBC:
             // "dopo l'insert, dammi l'id autogenerato dalla colonna AUTO_INCREMENT"
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, numPlayers);
            statement.executeUpdate();

            // Recupera l'id generato dal DB per questa partita
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                int id =  keys.getInt(1);
                System.out.println("[DB] Match salvato con id: " + id);
                return id;
            }


        } catch (SQLException e) {
            System.err.println("DB error saving match: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int numPlayers) {
        // NUOVA QUERY: Filtra chi è arrivato 1°, raggruppa per nome e conta le occorrenze
        String sql = """
                SELECT mr.nickname, COUNT(mr.match_id) AS total_wins
                FROM MATCH_RESULTS mr
                JOIN MATCHES m ON mr.match_id = m.id
                WHERE m.num_players = ? AND mr.final_position = 1
                GROUP BY mr.nickname
                ORDER BY total_wins DESC
                """;

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, numPlayers);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                // ATTENZIONE AL DTO: La data della partita qui non ha più senso,
                // perché stiamo unendo decine di partite diverse in una sola riga!
                // Usiamo il campo 'score' del tuo DTO per trasportare il numero di vittorie.
                leaderboard.add(new LeaderboardEntry(
                        rs.getString("nickname"),
                        rs.getInt("total_wins")
                ));
            }
        } catch (SQLException e) {
            System.err.println("DB error getting leaderboard: " + e.getMessage());
        }
        return leaderboard;
    }

    @Override
    public int getPlayerPosition(String nickname, int numPlayers) {
        String sql = """
                WITH WinCounts AS (
                    SELECT mr.nickname, COUNT(mr.match_id) AS total_wins
                    FROM MATCH_RESULTS mr
                    JOIN MATCHES m ON mr.match_id = m.id
                    WHERE m.num_players = ? AND mr.final_position = 1
                    GROUP BY mr.nickname
                )
                SELECT COUNT(*) + 1 AS position
                FROM WinCounts
                WHERE total_wins > (
                    SELECT COUNT(mr2.match_id)
                    FROM MATCH_RESULTS mr2
                    JOIN MATCHES m2 ON mr2.match_id = m2.id
                    WHERE m2.num_players = ? AND mr2.final_position = 1 AND mr2.nickname = ?
                )
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            // Assegnazione dei parametri per i tre '?' presenti nella query
            statement.setInt(1, numPlayers);  // Parametro (WinCounts)
            statement.setInt(2, numPlayers);  // Parametro per la subquery specifica del giocatore
            statement.setString(3, nickname); // Parametro per identificare il giocatore nella subquery

            ResultSet rs = statement.executeQuery();
            if (rs.next()) return rs.getInt("position");

        } catch (SQLException e) {
            System.err.println("DB error getting position: " + e.getMessage());
        }
        return -1;
    }
}