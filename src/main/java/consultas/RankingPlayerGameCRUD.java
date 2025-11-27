package consultas;

import tablas.RankingPlayerGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RankingPlayerGameCRUD {

    private Connection connection;

    public RankingPlayerGameCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<RankingPlayerGame> getAllRankingPlayerGame() throws SQLException {
        List<RankingPlayerGame> rankingPlayerGameList = new ArrayList<>();
        String sql = "SELECT player_id, first_name, game_name, ranking, wins, losses FROM players_games_ranking";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                RankingPlayerGame rankingPlayerGame = new RankingPlayerGame(
                        resultSet.getInt("player_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("game_name"),
                        resultSet.getObject("ranking", Integer.class), // Usar getObject para Integer
                        resultSet.getInt("wins"),
                        resultSet.getInt("losses")
                );
                rankingPlayerGameList.add(rankingPlayerGame);
            }
        }
        return rankingPlayerGameList;
    }

    public RankingPlayerGame getRankingPlayerGame(int playerId, String gameName) throws SQLException {
        String sql = "SELECT player_id, first_name, game_name, ranking, wins, losses FROM players_games_ranking WHERE player_id = ? AND game_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, gameName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new RankingPlayerGame(
                            resultSet.getInt("player_id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("game_name"),
                            resultSet.getObject("ranking", Integer.class), // Usar getObject para Integer
                            resultSet.getInt("wins"),
                            resultSet.getInt("losses")
                    );
                }
            }
        }
        return null;
    }

    public boolean createRankingPlayerGame(RankingPlayerGame rankingPlayerGame) throws SQLException {
        String sql = "INSERT INTO players_games_ranking (player_id, game_name, ranking, wins, losses) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, rankingPlayerGame.getPlayerId());
            preparedStatement.setString(2, rankingPlayerGame.getGameName());
            preparedStatement.setObject(3, rankingPlayerGame.getRanking(), Types.INTEGER); // Usar setObject para Integer
            preparedStatement.setInt(4, rankingPlayerGame.getWins());
            preparedStatement.setInt(5, rankingPlayerGame.getLosses());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateRankingPlayerGame(RankingPlayerGame rankingPlayerGame) throws SQLException {
        String sql = "UPDATE players_games_ranking SET ranking = ?, wins = ?, losses = ? WHERE player_id = ? AND game_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, rankingPlayerGame.getRanking(), Types.INTEGER); // Usar setObject para Integer
            preparedStatement.setInt(2, rankingPlayerGame.getWins());
            preparedStatement.setInt(3, rankingPlayerGame.getLosses());
            preparedStatement.setInt(4, rankingPlayerGame.getPlayerId());
            preparedStatement.setString(5, rankingPlayerGame.getGameName());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteRankingPlayerGame(int playerId, String gameCode) throws SQLException {
        String sql = "DELETE FROM players_games_ranking WHERE player_id = ? AND game_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, gameName);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}