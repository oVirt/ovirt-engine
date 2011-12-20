package org.ovirt.engine.core.notifier.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.tools.common.db.JbossConnectionFactory;
import org.ovirt.engine.core.tools.common.db.JbossConnectionFactory.ConnectivityCredentials;

/**
 * Naive connection helper which provides connections for a client.<br>
 * If a connection was closed (either by timeout or by an external client) <br>
 * will try to obtain new connection.
 *
 */
public class ConnectionHelper {

    public static class NaiveConnectionHelperException extends Exception {
        private static final long serialVersionUID = 6694416120068778523L;

        public NaiveConnectionHelperException(String message) {
            super(message);
        }

        public NaiveConnectionHelperException(String message, Throwable cause) {
            super(message, cause);
        }
    };

    private static final LogCompat log = LogFactoryCompat.getLog(ConnectionHelper.class);
    private ConnectivityCredentials credentials = null;
    private Connection connection = null;

    public ConnectionHelper(Map<String, String> prop) throws NaiveConnectionHelperException {

        String dataSourceXmlFile = prop.get(NotificationProperties.AS_DATA_SOURCE);
        String loginConfigXmlFile = prop.get(NotificationProperties.AS_LOGIN_CONFIG);

        // if application server data source configuration files provided, extract connectivity out of them
        if (StringUtils.isNotEmpty(dataSourceXmlFile)) {
            try {
                log.debug("Obtaining database connectivity using application server configuration files.");
                credentials = JbossConnectionFactory.getConnectivityCredentials(dataSourceXmlFile, loginConfigXmlFile);
            } catch (XPathExpressionException e) {
	            throw new NaiveConnectionHelperException("Failed to parse application server data source configuration files", e);
	        }
        } else {
            log.debug("Obtaining database connectivity using parameters of service configuration files.");
            credentials = new ConnectivityCredentials();
            credentials.setDriverClassName(prop.get(NotificationProperties.DB_JDBC_DRIVER_CLASS));
            credentials.setUserName(prop.get(NotificationProperties.DB_USER_NAME));
            credentials.setConnectionUrl(prop.get(NotificationProperties.DB_CONNECTION_URL));
            credentials.setPassword(prop.get(NotificationProperties.DB_PASSWORD));
        }

        try {
            Class.forName(credentials.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new NaiveConnectionHelperException("Driver class is not found: "
                    + credentials.getDriverClassName(), e);
        } catch (NullPointerException e) {
            throw new NaiveConnectionHelperException("Driver class is not provided", e);
        }

        connection = getNewConnection();
    }

    /**
     * Returns a connection based on pre-supplied connectivity details if<br>
     * current connection is closed or corrupted
     * @return a connection to the database
     * @throws NaiveConnectionHelperException
     */
    public Connection getConnection() throws NaiveConnectionHelperException {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return connection;
                }
            } catch (SQLException e) {
                log.info("Connection is closed. Assigning new connection.", e);
            }
        }
        connection = getNewConnection();
        return connection;
    }

    /**
     * Obtains new connection by a given connectivity details
     *
     * @return
     * @throws NaiveConnectionHelperException
     */
    private Connection getNewConnection() throws NaiveConnectionHelperException {
        try {
            return DriverManager.getConnection(credentials.getConnectionUrl(),
                    credentials.getUserName(),
                    credentials.getPassword());
        } catch (SQLException e) {
            throw new NaiveConnectionHelperException("Failed to establish database connection", e);
        }
    }

    /**
     * Closes a DB connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
