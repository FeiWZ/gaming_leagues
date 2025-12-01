package consultas;

import tablas.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchCRUD {

    private Connection connection;

    public MatchCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<Object[]> getAllMatchesWithNames() throws SQLException {
        List<Object[]> data = new ArrayList<>();

        String SQL = "SELECT " +
                "m.match_id, " +
                "m.match_date, " +
                "m.result, " +
                "m.match_type, " +
                "g.game_name, " +
                "p1.first_name AS player1_name, " +
                "p2.first_name AS player2_name " +
                "FROM matches m " +
                "JOIN players p1 ON m.player_name_1 = p1.first_name " +
                "JOIN players p2 ON m.player_name_2 = p2.first_name " +
                "JOIN games g ON m.game_name = g.game_name " +
                "ORDER BY m.match_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("match_id"),
                        rs.getTimestamp("match_date"),
                        rs.getString("result"),
                        rs.getString("match_type"),
                        rs.getString("game_name"),
                        rs.getString("player1_name"), // Corregido: Usar player1_name
                        rs.getString("player2_name")  // Corregido: Usar player2_name
                };
                data.add(row);
            }
        }
        return data;
    }


    // ⬇️ INICIO DEL CÓDIGO AÑADIDO ⬇️

    public List<Match> getAllMatches() throws SQLException {
        List<Match> matches = new ArrayList<>();
        String SQL = "SELECT match_id, match_date, result, match_type, player_name_1, player_name_2, game_name FROM matches ORDER BY match_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Match match = new Match(
                        rs.getInt("match_id"),
                        rs.getTimestamp("match_date"),
                        rs.getString("result"),
                        rs.getString("match_type"),
                        rs.getString("player_name_1"),
                        rs.getString("player_name_2"),
                        rs.getString("game_name")
                );
                matches.add(match);
            }
        }
        return matches;
    }

    public Match getMatchById(int matchId) throws SQLException {
        // No es estrictamente necesario para la funcionalidad solicitada, pero se puede implementar si se requiere.
        return null;
    }

    public boolean createMatch(Match match) throws SQLException {
        String SQL = "INSERT INTO matches (match_id, match_date, result, match_type, player_name_1, player_name_2, game_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL)) {
            pstmt.setInt(1, match.getMatchId());
            pstmt.setTimestamp(2, match.getMatchDate());
            pstmt.setString(3, match.getResult());
            pstmt.setString(4, match.getMatchType());
            pstmt.setString(5, match.getPlayer1());
            pstmt.setString(6, match.getPlayer2());
            pstmt.setString(7, match.getGameCode());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateMatch(Match match) throws SQLException {
        String SQL = "UPDATE matches SET match_date = ?, result = ?, match_type = ?, player_name_1 = ?, player_name_2 = ?, game_name = ? WHERE match_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL)) {
            pstmt.setTimestamp(1, match.getMatchDate());
            pstmt.setString(2, match.getResult());
            pstmt.setString(3, match.getMatchType());
            pstmt.setString(4, match.getPlayer1());
            pstmt.setString(5, match.getPlayer2());
            pstmt.setString(6, match.getGameCode());
            pstmt.setInt(7, match.getMatchId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMatch(int matchId) throws SQLException {
        String SQL = "DELETE FROM matches WHERE match_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL)) {
            pstmt.setInt(1, matchId);
            return pstmt.executeUpdate() > 0;
        }
    }
    // ⬆️ FIN DEL CÓDIGO AÑADIDO ⬆️
}