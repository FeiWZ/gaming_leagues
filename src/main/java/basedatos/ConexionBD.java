package basedatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/game_league_db";
    private static final String USER = "daniela";
    private static final String PASSWORD = "lapecha25";

    public static Connection conectar() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos:");
            e.printStackTrace();
        }
        return conexion;
    }

    public static void main(String[] args) {
        conectar();
    }
}
