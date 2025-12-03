package consultas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transacciones {

    public static boolean registrarPartidoCompleto(Connection conn,
                                                   int matchId, String player1, String player2,
                                                   String gameName, String resultado) throws SQLException {

        boolean exito = false;
        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String sqlPartido = "INSERT INTO matches (match_id, player_name_1, player_name_2, game_name, result, match_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            try (PreparedStatement psPartido = conn.prepareStatement(sqlPartido)) {
                psPartido.setInt(1, matchId);
                psPartido.setString(2, player1);
                psPartido.setString(3, player2);
                psPartido.setString(4, gameName);
                psPartido.setString(5, resultado);
                psPartido.executeUpdate();
            }

            if (!resultado.contains("Empate")) {
                String ganador = resultado.contains("1") ? player1 : player2;
                String perdedor = resultado.contains("1") ? player2 : player1;

                String sqlGanador = "UPDATE players_games_ranking SET wins = wins + 1 WHERE player_name = ? AND game_name = ?";
                try (PreparedStatement psGanador = conn.prepareStatement(sqlGanador)) {
                    psGanador.setString(1, ganador);
                    psGanador.setString(2, gameName);
                    psGanador.executeUpdate();
                }

                String sqlPerdedor = "UPDATE players_games_ranking SET losses = losses + 1 WHERE player_name = ? AND game_name = ?";
                try (PreparedStatement psPerdedor = conn.prepareStatement(sqlPerdedor)) {
                    psPerdedor.setString(1, perdedor);
                    psPerdedor.setString(2, gameName);
                    psPerdedor.executeUpdate();
                }
            }

            conn.commit();
            exito = true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }

        return exito;
    }

    public static boolean transferirJugador(Connection conn,
                                            String playerName, int equipoOrigenId, int equipoDestinoId)
            throws SQLException {

        boolean exito = false;
        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String sqlSalir = "UPDATE teams SET date_to = CURRENT_DATE WHERE player_name = ? AND team_id = ? AND date_to IS NULL";
            try (PreparedStatement psSalir = conn.prepareStatement(sqlSalir)) {
                psSalir.setString(1, playerName);
                psSalir.setInt(2, equipoOrigenId);
                psSalir.executeUpdate();
            }

            String sqlUnirse = "INSERT INTO teams (team_id, player_name, date_from, role) VALUES (?, ?, CURRENT_DATE, 'Jugador')";
            try (PreparedStatement psUnirse = conn.prepareStatement(sqlUnirse)) {
                psUnirse.setInt(1, equipoDestinoId);
                psUnirse.setString(2, playerName);
                psUnirse.executeUpdate();
            }

            String sqlValidar = "SELECT COUNT(*) FROM teams WHERE player_name = ? AND date_to IS NULL";
            try (PreparedStatement psValidar = conn.prepareStatement(sqlValidar)) {
                psValidar.setString(1, playerName);
                var rs = psValidar.executeQuery();
                if (rs.next() && rs.getInt(1) > 3) {
                    throw new SQLException("El jugador no puede estar en mas de 3 equipos simultaneamente");
                }
            }

            conn.commit();
            exito = true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }

        return exito;
    }

    public static boolean eliminarJugadorConDependencias(Connection conn, int playerId)
            throws SQLException {

        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String sqlRankings = "DELETE FROM players_games_ranking WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlRankings)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }

            String sqlEquipos = "DELETE FROM teams WHERE player_name IN (SELECT first_name FROM players WHERE player_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlEquipos)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }

            String sqlMatches1 = "DELETE FROM matches WHERE player_name_1 IN (SELECT first_name FROM players WHERE player_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlMatches1)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }

            String sqlMatches2 = "DELETE FROM matches WHERE player_name_2 IN (SELECT first_name FROM players WHERE player_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlMatches2)) {
                ps.setInt(1, playerId);
                ps.executeUpdate();
            }

            String sqlJugador = "DELETE FROM players WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlJugador)) {
                ps.setInt(1, playerId);
                int eliminado = ps.executeUpdate();

                if (eliminado > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }
    }

    public static boolean crearLigaCompleta(Connection conn,
                                            String leagueName, String details, String gameName,
                                            int prizePool, String rules, java.sql.Date startDate,
                                            java.sql.Date endDate) throws SQLException {

        boolean exito = false;
        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String sqlliga = "INSERT INTO leagues (league_name, league_details, game_name, prize_pool, rules, started_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlliga)) {
                ps.setString(1, leagueName);
                ps.setString(2, details);
                ps.setString(3, gameName);
                ps.setInt(4, prizePool);
                ps.setString(5, rules);
                ps.setDate(6, startDate);
                ps.setDate(7, endDate);
                ps.executeUpdate();
            }

            conn.commit();
            exito = true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }

        return exito;
    }
}