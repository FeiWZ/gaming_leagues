package org.example.consultas;

import org.example.tablas.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamCRUD {

    private Connection connection;

    public TeamCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<Team> getAllTeams() throws SQLException {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT team_id, team_name, date_created, date_disbanded, id_coach, created_by_player_id FROM teams";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Team team = new Team(
                        resultSet.getInt("team_id"),
                        resultSet.getString("team_name"),
                        resultSet.getDate("date_created"),
                        resultSet.getDate("date_disbanded"),
                        resultSet.getInt("id_coach"),
                        resultSet.getInt("created_by_player_id")
                );
                teams.add(team);
            }
        }
        return teams;
    }

    public Team getTeamById(int teamId) throws SQLException {
        String sql = "SELECT team_id, team_name, date_created, date_disbanded, id_coach, created_by_player_id FROM teams WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Team(
                            resultSet.getInt("team_id"),
                            resultSet.getString("team_name"),
                            resultSet.getDate("date_created"),
                            resultSet.getDate("date_disbanded"),
                            resultSet.getInt("id_coach"),
                            resultSet.getInt("created_by_player_id")
                    );
                }
            }
        }
        return null;
    }

    public boolean createTeam(Team team) throws SQLException {
        String sql = "INSERT INTO teams (team_name, date_created, date_disbanded, id_coach, created_by_player_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setInt(5, team.getCreatedByPlayerId());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateTeam(Team team) throws SQLException {
        String sql = "UPDATE teams SET team_name = ?, date_created = ?, date_disbanded = ?, id_coach = ?, created_by_player_id = ? WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setInt(5, team.getCreatedByPlayerId());
            preparedStatement.setInt(6, team.getTeamId());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteTeam(int teamId) throws SQLException {
        String sql = "DELETE FROM teams WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamId);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}