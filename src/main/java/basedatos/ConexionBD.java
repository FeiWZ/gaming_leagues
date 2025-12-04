package basedatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/BD_GameLeagues";
    private static final String USER = "Empleados";
    private static final String PASSWORD = "hola123";
    private static Connection conexion = null;

    public static Connection conectar() {
        if (conexion == null) {
            try {
                Properties props = new Properties();
                props.setProperty("user", USER);
                props.setProperty("password", PASSWORD);
                props.setProperty("ssl", "false");

                conexion = DriverManager.getConnection(URL, props);
                conexion.setAutoCommit(true);

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void iniciarTransaccion() throws SQLException {
        if (conexion != null && conexion.getAutoCommit()) {
            conexion.setAutoCommit(false);
        }
    }

    public static void confirmarTransaccion() throws SQLException {
        if (conexion != null && !conexion.getAutoCommit()) {
            conexion.commit();
            conexion.setAutoCommit(true);
        }
    }

    public static void cancelarTransaccion() throws SQLException {
        if (conexion != null && !conexion.getAutoCommit()) {
            conexion.rollback();
            conexion.setAutoCommit(true);
        }
    }

    public static void main(String[] args) {
        Connection conn = conectar();
        if (conn != null) {
            cerrarConexion();
        }
    }
}