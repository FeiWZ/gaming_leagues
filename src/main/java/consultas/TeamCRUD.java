package consultas;

import tablas.Team;

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
        String sql = "SELECT team_id, team_name, date_created, date_disbanded, id_coach, created_by_player FROM teams";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Team team = new Team(
                        resultSet.getInt("team_id"),
                        resultSet.getString("team_name"),
                        resultSet.getDate("date_created"),
                        resultSet.getDate("date_disbanded"),
                        resultSet.getInt("id_coach"),
                        resultSet.getString("created_by_player")
                );
                teams.add(team);
            }
        }
        return teams;
    }

    public boolean teamExists(String teamName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams WHERE team_name = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, teamName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Retorna true si encuentra al menos 1 equipo
                }
            }
        }
        return false;
    }

    public boolean createTeam(Team team) throws SQLException {
        String sql = "INSERT INTO teams (team_name, date_created, date_disbanded, id_coach, created_by_player) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setString(5, team.getCreatedByPlayer());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateTeam(Team team) throws SQLException {
        String sql = "UPDATE teams SET team_name = ?, date_created = ?, date_disbanded = ?, id_coach = ?, created_by_player = ? WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setString(5, team.getCreatedByPlayer());
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