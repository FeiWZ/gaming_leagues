package consultas;

import tablas.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerCRUD {

    private Connection connection;

    public PlayerCRUD(Connection connection) {
        this.connection = connection;
    }

    public List<Player> getAllPlayers() throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT player_id, first_name, last_name, gender, address, nationality, birthdate, email FROM players";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Player player = new Player(
                        resultSet.getInt("player_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("gender"),
                        resultSet.getString("address"),
                        resultSet.getString("nationality"),
                        resultSet.getDate("birthdate"),
                        resultSet.getString("email")
                );
                players.add(player);
            }
        }
        return players;
    }

    public Player getPlayerById(int playerId) throws SQLException {
        String sql = "SELECT player_id, first_name, last_name, gender, address, nationality, birthdate, email FROM players WHERE player_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, playerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Player(
                            resultSet.getInt("player_id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("gender"),
                            resultSet.getString("address"),
                            resultSet.getString("nationality"),
                            resultSet.getDate("birthdate"),
                            resultSet.getString("email")
                    );
                }
            }
        }
        return null;
    }

    public boolean createPlayer(Player player) throws SQLException {
        String sql = "INSERT INTO players (first_name, last_name, gender, address, nationality, birthdate, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getFirstName());
            preparedStatement.setString(2, player.getLastName());
            preparedStatement.setString(3, player.getGender());
            preparedStatement.setString(4, player.getAddress());
            preparedStatement.setString(5, player.getNationality());
            preparedStatement.setDate(6, new Date(player.getBirthdate().getTime()));
            preparedStatement.setString(7, player.getEmail());

            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updatePlayer(Player player) throws SQLException {
        String sql = "UPDATE players SET first_name = ?, last_name = ?, gender = ?, address = ?, nationality = ?, birthdate = ?, email = ? WHERE player_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getFirstName());
            preparedStatement.setString(2, player.getLastName());
            preparedStatement.setString(3, player.getGender());
            preparedStatement.setString(4, player.getAddress());
            preparedStatement.setString(5, player.getNationality());
            preparedStatement.setDate(6, new Date(player.getBirthdate().getTime()));
            preparedStatement.setString(7, player.getEmail());
            preparedStatement.setInt(8, player.getPlayerId());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }
    public boolean deletePlayer(int playerId) throws SQLException {
        String sql = "DELETE FROM players WHERE player_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, playerId);

            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}