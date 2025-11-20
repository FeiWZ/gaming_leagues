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
                "g.name AS game_name, " +
                "p1.player_name AS player1_name, " +
                "p2.player_name AS player2_name " +
                "FROM matches m " +
                "JOIN players p1 ON m.player_id_1 = p1.player_id " +
                "JOIN players p2 ON m.player_id_2 = p2.player_id " +
                "JOIN games g ON m.game_code = g.game_code " +
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
                        rs.getString("player1_name"),
                        rs.getString("player2_name")
                };
                data.add(row);
            }
        }
        return data;
    }


    public List<Match> getAllMatches() throws SQLException { /* ... */ return new ArrayList<>(); }
    public Match getMatchById(int matchId) throws SQLException { /* ... */ return null; }
    public boolean createMatch(Match match) throws SQLException { /* ... */ return false; }
    public boolean updateMatch(Match match) throws SQLException { /* ... */ return false; }
    public boolean deleteMatch(int matchId) throws SQLException { /* ... */ return false; }
}