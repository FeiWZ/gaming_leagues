package consultas;

import tablas.League;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeagueCRUD {

    private Connection connection;

    public LeagueCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<League> getAllLeagues() throws SQLException {
        List<League> leagues = new ArrayList<>();
        String sql = "SELECT league_id, league_name, league_details, prize_pool, rules, started_date, end_date FROM leagues";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                League league = new League(
                        resultSet.getInt("league_id"),
                        resultSet.getString("league_name"),
                        resultSet.getString("league_details"),
                        resultSet.getInt("prize_pool"),
                        resultSet.getString("rules"),
                        resultSet.getDate("started_date"),
                        resultSet.getDate("end_date")
                );
                leagues.add(league);
            }
        }
        return leagues;
    }

    public League getLeagueById(int leagueId) throws SQLException {
        String sql = "SELECT league_id, league_name, league_details, prize_pool, rules, started_date, end_date FROM leagues WHERE league_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, leagueId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new League(
                            resultSet.getInt("league_id"),
                            resultSet.getString("league_name"),
                            resultSet.getString("league_details"),
                            resultSet.getInt("prize_pool"),
                            resultSet.getString("rules"),
                            resultSet.getDate("started_date"),
                            resultSet.getDate("end_date")
                    );
                }
            }
        }
        return null;
    }

    public boolean createLeague(League league) throws SQLException {
        String sql = "INSERT INTO leagues (league_name, league_details, prize_pool, rules, started_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, league.getLeagueName());
            preparedStatement.setString(2, league.getLeagueDetails());
            preparedStatement.setInt(3, league.getPrizePool());
            preparedStatement.setString(4, league.getRules());
            preparedStatement.setDate(5, new Date(league.getStartedDate().getTime()));
            preparedStatement.setDate(6, new Date(league.getEndDate().getTime()));

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateLeague(League league) throws SQLException {
        String sql = "UPDATE leagues SET league_name = ?, league_details = ?, prize_pool = ?, rules = ?, started_date = ?, end_date = ? WHERE league_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, league.getLeagueName());
            preparedStatement.setString(2, league.getLeagueDetails());
            preparedStatement.setInt(3, league.getPrizePool());
            preparedStatement.setString(4, league.getRules());
            preparedStatement.setDate(5, new Date(league.getStartedDate().getTime()));
            preparedStatement.setDate(6, new Date(league.getEndDate().getTime()));
            preparedStatement.setInt(7, league.getLeagueId());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteLeague(int leagueId) throws SQLException {
        String sql = "DELETE FROM leagues WHERE league_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, leagueId);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}