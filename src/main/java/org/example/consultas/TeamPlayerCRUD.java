package org.example.consultas;

import org.example.tablas.TeamPlayer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamPlayerCRUD {

    private Connection connection;

    public TeamPlayerCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<TeamPlayer> getAllTeamPlayer() throws SQLException {
        List<TeamPlayer> teamPlayerList = new ArrayList<>();
        String sql = "SELECT team_id, player_id, date_from, date_to, role FROM team_players";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                TeamPlayer teamPlayer = new TeamPlayer(
                        resultSet.getInt("team_id"),
                        resultSet.getInt("player_id"),
                        resultSet.getDate("date_from"),
                        resultSet.getDate("date_to"),
                        resultSet.getString("role")
                );
                teamPlayerList.add(teamPlayer);
            }
        }
        return teamPlayerList;
    }

    public List<TeamPlayer> getTeamPlayerByTeamId(int teamId) throws SQLException {
        List<TeamPlayer> teamPlayerList = new ArrayList<>();
        String sql = "SELECT team_id, player_id, date_from, date_to, role FROM team_players WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    TeamPlayer teamPlayer = new TeamPlayer(
                            resultSet.getInt("team_id"),
                            resultSet.getInt("player_id"),
                            resultSet.getDate("date_from"),
                            resultSet.getDate("date_to"),
                            resultSet.getString("role")
                    );
                    teamPlayerList.add(teamPlayer);
                }
            }
        }
        return teamPlayerList;
    }

    public boolean createTeamPlayer(TeamPlayer teamPlayer) throws SQLException {
        String sql = "INSERT INTO team_players (team_id, player_id, date_from, date_to, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamPlayer.getTeamId());
            preparedStatement.setInt(2, teamPlayer.getPlayerId());

            // Convertir java.util.Date a java.sql.Date
            java.sql.Date dateFromSql = (teamPlayer.getDateFrom() != null) ? new java.sql.Date(teamPlayer.getDateFrom().getTime()) : null;
            java.sql.Date dateToSql = (teamPlayer.getDateTo() != null) ? new java.sql.Date(teamPlayer.getDateTo().getTime()) : null;

            preparedStatement.setDate(3, dateFromSql);
            preparedStatement.setDate(4, dateToSql);
            preparedStatement.setString(5, teamPlayer.getRole());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateTeamPlayer(TeamPlayer teamPlayer) throws SQLException {
        String sql = "UPDATE team_players SET date_from = ?, date_to = ?, role = ? WHERE team_id = ? AND player_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Convertir java.util.Date a java.sql.Date
            java.sql.Date dateFromSql = (teamPlayer.getDateFrom() != null) ? new java.sql.Date(teamPlayer.getDateFrom().getTime()) : null;
            java.sql.Date dateToSql = (teamPlayer.getDateTo() != null) ? new java.sql.Date(teamPlayer.getDateTo().getTime()) : null;

            preparedStatement.setDate(1, dateFromSql);
            preparedStatement.setDate(2, dateToSql);
            preparedStatement.setString(3, teamPlayer.getRole());
            preparedStatement.setInt(4, teamPlayer.getTeamId());
            preparedStatement.setInt(5, teamPlayer.getPlayerId());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteTeamPlayer(int teamId, int playerId) throws SQLException {
        String sql = "DELETE FROM team_players WHERE team_id = ? AND player_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamId);
            preparedStatement.setInt(2, playerId);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}