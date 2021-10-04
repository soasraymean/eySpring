package by.dkozyrev.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Class handles connection to DB
public class ConnectionClass {
    private static ConnectionClass INSTANCE;
    private static Connection connection;

    private ConnectionClass() {
        connection = raiseConnection();
    }

    public static ConnectionClass getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectionClass();
        }
        return INSTANCE;
    }

    private Connection raiseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(Config.INSTANCE.getDbUrl(), Config.INSTANCE.getDbUser(), Config.INSTANCE.getDbPassword());
        } catch (SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void shutdown() {
        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
