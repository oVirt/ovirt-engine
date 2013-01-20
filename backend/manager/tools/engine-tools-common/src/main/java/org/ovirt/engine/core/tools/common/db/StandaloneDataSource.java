package org.ovirt.engine.core.tools.common.db;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;

public class StandaloneDataSource implements DataSource {
    // The log:
    private final static Logger log = Logger.getLogger(StandaloneDataSource.class);

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
        String configPath = System.getenv("ENGINE_VARS");
        if (configPath == null) {
            configPath = "/etc/sysconfig/ovirt-engine";
        }
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new SQLException("Can't find engine configuration file \"" + configFile.getAbsolutePath() + "\".");
        }
        Properties config = new Properties();
        FileReader configReader = null;
        try {
            configReader = new FileReader(configFile);
            config.load(configReader);
        }
        catch (IOException exception) {
            throw new SQLException("Can't load engine configuration file \"" + configFile.getAbsolutePath() + "\".", exception);
        }
        finally {
            try {
                configReader.close();
            }
            catch (IOException exception) {
                log.warn("Strange, failed to close file \"" + configFile.getAbsolutePath() + "\".", exception);
            }
        }

        // Get the database connection details:
        driver = config.getProperty(ENGINE_DB_DRIVER);
        if (driver == null) {
            throw new SQLException("Can't find database driver parameter \"" + ENGINE_DB_DRIVER + "\" in file \"" + configFile.getAbsolutePath() + "\".");
        }
        url = config.getProperty(ENGINE_DB_URL);
        if (url == null) {
            throw new SQLException("Can't find database URL parameter \"" + ENGINE_DB_URL + "\" in file \"" + configFile.getAbsolutePath() + "\".");
        }
        user = config.getProperty("ENGINE_DB_USER");
        if (user == null) {
            throw new SQLException("Can't find database user name parameter \"" + ENGINE_DB_USER + "\" in file \"" + configFile.getAbsolutePath() + "\".");
        }
        password = config.getProperty("ENGINE_DB_PASSWORD");
        if (password == null) {
            throw new SQLException("Can't find database password parameter \"" + ENGINE_DB_PASSWORD + "\" in file \"" + configFile.getAbsolutePath() + "\".");
        }

        // The password is encrypted inside the file, so we need to decrypt it here:
        password = EncryptionUtils.decode(password, "", "");
        if (password == null) {
            throw new SQLException("Failed to decrypt password from parameter \"" + ENGINE_DB_PASSWORD + "\" in file \"" + configFile.getAbsolutePath() + "\".");
        }

        // Load the driver:
        try {
            Class.forName(driver);
        }
        catch (ClassNotFoundException exception) {
            throw new SQLException("Failed to load database driver \"" + driver + "\".", exception);
        }

        // Open the connection:
        openConnection();

        // Register a shutdown hook to close the connection when finishing:
        Runtime.getRuntime().addShutdownHook(
           new Thread() {
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
            new InvocationHandler() {
                public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("close")) {
                        return null;
                    }
                    return method.invoke(connection, args);
                }
            }
        );
    }

    private void closeConnection () {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (SQLException exception) {
            log.error("Can't close connection.", exception);
        }
        connection = null;
        wrapper = null;
    }

    private void checkConnection () throws SQLException {
        if (!connection.isValid(0)) {
            throw new SQLException("The connection has been closed or invalid.");
        }
    }

    public Connection getConnection () throws SQLException {
        try {
            checkConnection();
        }
        catch (SQLException exception) {
            log.warn("Can't check the connection, will close it and open a new one.", exception);
            closeConnection();
            openConnection();
        }
        return wrapper;
    }

    public Connection getConnection (String user, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public PrintWriter getLogWriter () throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public void setLogWriter (PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public void setLoginTimeout (int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public int getLoginTimeout () throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public <T> T unwrap (Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public boolean isWrapperFor (Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public java.util.logging.Logger getParentLogger () throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
