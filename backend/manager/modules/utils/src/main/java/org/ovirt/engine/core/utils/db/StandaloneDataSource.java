package org.ovirt.engine.core.utils.db;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneDataSource implements DataSource {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(StandaloneDataSource.class);

    // Names of the properties expected in the engine configuration file:
    private static final String ENGINE_DB_DRIVER = "ENGINE_DB_DRIVER";
    private static final String ENGINE_DB_URL = "ENGINE_DB_URL";
    private static final String ENGINE_DB_USER = "ENGINE_DB_USER";
    private static final String ENGINE_DB_PASSWORD = "ENGINE_DB_PASSWORD";

    // The database connection details loaded from the configuration file:
    private String driver;
    private String url;
    private String user;
    private String password;

    // We open and reuse a single connection and we wrap it so that is not
    // closed when the client calls the close method:
    private Connection connection;
    private Connection wrapper;

    public StandaloneDataSource() throws SQLException {
        // Load the service configuration file:
        EngineLocalConfig config = EngineLocalConfig.getInstance();

        // Get the database connection details:
        driver = config.getProperty(ENGINE_DB_DRIVER);
        url = config.getProperty(ENGINE_DB_URL);
        user = config.getProperty(ENGINE_DB_USER);
        password = config.getProperty(ENGINE_DB_PASSWORD);

        // Load the driver:
        try {
            Class.forName(driver);
        } catch(ClassNotFoundException exception) {
            throw new SQLException("Failed to load database driver \"" + driver + "\".", exception);
        }

        // Open the connection:
        openConnection();

        // Register a shutdown hook to close the connection when finishing:
        Runtime.getRuntime().addShutdownHook(
           new Thread() {
               @Override
               public void run() {
                   closeConnection();
               }
           }
        );
    }

    private void openConnection () throws SQLException {
        // Open the connection:
        connection = DriverManager.getConnection(url, user, password);

        // Wrap the connection so that it is not closed when the client
        // calls the close method:
        wrapper = (Connection) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { Connection.class },
            (proxy, method, args) -> {
                if (method.getName().equals("close")) {
                    return null;
                }
                return method.invoke(connection, args);
            }
        );
    }

    private void closeConnection () {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch(SQLException exception) {
            log.error("Can't close connection.", exception);
        }
        connection = null;
        wrapper = null;
    }

    /**
     * test the connection by executing a simple query..
     *
     * Note: assuming connection is not null.
     *
     * @throws SQLException  if a database access error occurs
     * or this method is called on a closed connection.
     */
    private void checkConnection() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select null")) {
            rs.next();
        }
    }

    @Override
    public Connection getConnection () throws SQLException {
        try {
            if (connection == null) {
                openConnection();
            }
            checkConnection();
        } catch(SQLException exception) {
            log.warn("Can't check the connection, will close it and open a new one.", exception);
            closeConnection();
            openConnection();
        }
        return wrapper;
    }

    @Override
    public Connection getConnection (String user, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public PrintWriter getLogWriter () throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLogWriter (PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout (int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getLoginTimeout () throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap (Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor (Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public java.util.logging.Logger getParentLogger () throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
