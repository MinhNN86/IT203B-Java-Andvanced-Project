package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDao {
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
}
