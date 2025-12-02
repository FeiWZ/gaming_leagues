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
        String sql = "SELECT team_id, team_name, date_created, date_disbanded, id_coach, created_by_player, player_name, date_from, date_to, role FROM teams";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Team team = new Team(
                        resultSet.getInt("team_id"),
                        resultSet.getString("team_name"),
                        resultSet.getDate("date_created"),
                        resultSet.getDate("date_disbanded"),
                        resultSet.getInt("id_coach"),
                        resultSet.getString("created_by_player"),
                        resultSet.getString("player_name"),
                        resultSet.getDate("date_from"),
                        resultSet.getDate("date_to"),
                        resultSet.getString("role")
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
        String sql = "INSERT INTO teams (team_name, date_created, date_disbanded, id_coach, created_by_player, player_name, date_from, date_to, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setString(5, team.getCreatedByPlayer());
            preparedStatement.setString(6, team.getPlayerName());

            // Convertir java.util.Date a java.sql.Date
            java.sql.Date dateFromSql = (team.getDateFrom() != null) ? new java.sql.Date(team.getDateFrom().getTime()) : null;
            java.sql.Date dateToSql = (team.getDateTo() != null) ? new java.sql.Date(team.getDateTo().getTime()) : null;

            preparedStatement.setDate(7, dateFromSql);
            preparedStatement.setDate(8, dateToSql);
            preparedStatement.setString(9, team.getRole());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateTeam(Team team) throws SQLException {
        String sql = "UPDATE teams SET team_name = ?, date_created = ?, date_disbanded = ?, id_coach = ?, created_by_player = ?, player_name = ?, date_from = ?, date_to = ?, role = ? WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setDate(2, new Date(team.getDateCreated().getTime()));
            preparedStatement.setDate(3, team.getDateDisbanded() != null ? new Date(team.getDateDisbanded().getTime()) : null);
            preparedStatement.setInt(4, team.getIdCoach());
            preparedStatement.setString(5, team.getCreatedByPlayer());
            preparedStatement.setString(6, team.getPlayerName());

            // Convertir java.util.Date a java.sql.Date
            java.sql.Date dateFromSql = (team.getDateFrom() != null) ? new java.sql.Date(team.getDateFrom().getTime()) : null;
            java.sql.Date dateToSql = (team.getDateTo() != null) ? new java.sql.Date(team.getDateTo().getTime()) : null;

            preparedStatement.setDate(7, dateFromSql);
            preparedStatement.setDate(8, dateToSql);
            preparedStatement.setString(9, team.getRole());
            preparedStatement.setInt(10, team.getTeamId());

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