package consultas;

import tablas.GameLeague;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameLeagueCRUD {

    private Connection connection;

    public GameLeagueCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<GameLeague> getAllLeaguesGames() throws SQLException {
        List<GameLeague> leaguesGamesList = new ArrayList<>();
        String sql = "SELECT league_id, game_code FROM leagues_games";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                GameLeague leagueGame = new GameLeague(
                        resultSet.getInt("league_id"),
                        resultSet.getString("game_code")
                );
                leaguesGamesList.add(leagueGame);
            }
        }
        return leaguesGamesList;
    }

    public List<GameLeague> getLeaguesGamesByLeagueId(int leagueId) throws SQLException {
        List<GameLeague> leaguesGamesList = new ArrayList<>();
        String sql = "SELECT league_id, game_code FROM leagues_games WHERE league_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, leagueId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    GameLeague leagueGame = new GameLeague(
                            resultSet.getInt("league_id"),
                            resultSet.getString("game_code")
                    );
                    leaguesGamesList.add(leagueGame);
                }
            }
        }
        return leaguesGamesList;
    }

    public boolean createLeaguesGames(GameLeague leaguesGames) throws SQLException {
        String sql = "INSERT INTO leagues_games (league_id, game_code) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, leaguesGames.getLeagueId());
            preparedStatement.setString(2, leaguesGames.getGameCode());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean deleteLeaguesGames(int leagueId, String gameCode) throws SQLException {
        String sql = "DELETE FROM leagues_games WHERE league_id = ? AND game_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, leagueId);
            preparedStatement.setString(2, gameCode);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}