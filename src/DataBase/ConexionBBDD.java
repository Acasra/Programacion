package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBBDD {
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String USER = "system";
    private static final String PASSWORD = "Margarita2013.";

        public static Connection getConnection() throws SQLException {
            try {
                // Cargar el driver JDBC
                Class.forName("oracle.jdbc.OracleDriver");
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC no encontrado", e);
            }
        }
    }