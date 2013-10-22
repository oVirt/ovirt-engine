package org.ovirt.engine.core.utils.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper methods to handle DB related issues.
 * This class will be deprecated once oVirt gets compiled with
 * JDK7 compliance as Connection, ResultSet and Statement extend
 * AutoClosable
 */
public class DbUtils {

    public static void closeQuietly(ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void closeQuietly(ResultSet resultSet, Connection connection) {
       closeQuietly(resultSet, null, connection);
    }

    public static void closeQuietly(Statement statement, Connection connection) {
        closeQuietly(null, statement, connection);
    }

    public static void closeQuietly(ResultSet resultSet, Statement statement, Connection connection) {
        closeQuietly(resultSet);
        closeQuietly(statement);
        closeQuietly(connection);
    }

}
