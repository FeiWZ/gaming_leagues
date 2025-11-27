package consultas;

import tablas.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameCRUD {

    private Connection connection;

    public GameCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<String> getAllGameCodes() throws SQLException {
        List<String> codes = new ArrayList<>();
        String sql = "SELECT game_code FROM games";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                codes.add(resultSet.getString("game_code"));
            }
        }
        return codes;
    }

    public List<String> getAllGameNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT game_name FROM games";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                names.add(resultSet.getString("game_name"));
            }
        }
        return names;
    }

    public List<Game> getAllGames() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT game_code, game_name, game_description, game_genres FROM games";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Game game = new Game(
                        resultSet.getString("game_code"),
                        resultSet.getString("game_name"),
                        resultSet.getString("game_description"),
                        resultSet.getString("game_genres")
                );
                games.add(game);
            }
        }
        return games;
    }

    public Game getGameByCode(String gameCode) throws SQLException {
        String sql = "SELECT game_code, game_name, game_description, game_genres FROM games WHERE game_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, gameCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Game(
                            resultSet.getString("game_code"),
                            resultSet.getString("game_name"),
                            resultSet.getString("game_description"),
                            resultSet.getString("game_genres")
                    );
                }
            }
        }
        return null;
    }

    public boolean createGame(Game game) throws SQLException {
        String sql = "INSERT INTO games (game_code, game_name, game_description, game_genres) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, game.getGameCode());
            preparedStatement.setString(2, game.getGameName());
            preparedStatement.setString(3, game.getGameDescription());
            preparedStatement.setString(4, game.getGameGenres());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateGame(Game game) throws SQLException {
        String sql = "UPDATE games SET game_name = ?, game_description = ?, game_genres = ? WHERE game_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, game.getGameName());
            preparedStatement.setString(2, game.getGameDescription());
            preparedStatement.setString(3, game.getGameGenres());
            preparedStatement.setString(4, game.getGameCode());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteGame(String gameCode) throws SQLException {
        String sql = "DELETE FROM games WHERE game_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, gameCode);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}