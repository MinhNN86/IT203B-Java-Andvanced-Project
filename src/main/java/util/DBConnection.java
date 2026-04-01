package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/prj_meeting_java_05?createDatabaseIfNotExist=true&serverTimezone=Asia/Ho_Chi_Minh";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static DBConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
