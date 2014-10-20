package org.ovirt.engine.core.dal.dbbroker;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

/**
 * A locator singleton for looking up (and initializing) DbFacade instance
 */
public class DbFacadeLocator {
    private static final Logger log = LoggerFactory.getLogger(DbFacadeLocator.class);

    // Default values for the configuration (these will be replaced with the
    // values from the configuration file):
    private static final int DEFAULT_CHECK_INTERVAL = 5000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    // Time to wait between checks of the database connection and maximum time
    // to wait for a connection:
    private static int checkInterval = DEFAULT_CHECK_INTERVAL;
    private static int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    // The facade singleton:
    private static volatile DbFacade dbFacade;

    /**
     * Lazily create and configure the facade.
     *
     * @return the reference to the facade if it was successfully created or
     *   <code>null</code> if something failed
     */
    public static DbFacade getDbFacade() {
        if (dbFacade == null) {
            synchronized(DbFacadeLocator.class) {
                if (dbFacade == null) {
                    dbFacade = createDbFacade();
                }
            }
        }
        return dbFacade;
    }

    /**
     * Create and configure the facade.
     *
     * @return the reference to the facade if it was successfully created or
     *   <code>null</code> if something failed
     */
    private static DbFacade createDbFacade() {
        // Load the configuration:
        loadDbFacadeConfig();

        // Locate the data source:
        DataSource ds = locateDataSource();
        if (ds == null) {
            return null;
        }

        // Load the dialect:
        DbEngineDialect dialect = loadDbEngineDialect();

        // Create and configure the facade:
        DbFacade facade = new DbFacade();
        facade.setOnStartConnectionTimeout(connectionTimeout);
        facade.setConnectionCheckInterval(checkInterval);
        facade.setDbEngineDialect(dialect);
        JdbcTemplate template = dialect.createJdbcTemplate(ds);
        SQLErrorCodeSQLExceptionTranslator tr = new CustomSQLErrorCodeSQLExceptionTranslator(ds);
        template.setExceptionTranslator(tr);
        facade.setTemplate(template);

        // Return the new facade:
        return facade;
    }

    /**
     * Locate the data source. It will try to locate the data source repeatedly
     * till it succeeds or till it takes longer than the time out.
     *
     * @return the data source if it was located or <code>null</code> if it
     *   couldn't be located
     */
    private static DataSource locateDataSource() {
        // We don't wait forever for the data source, at most the value of
        // the connection timeout parameter, so we need to remember when we
        // started to wait:
        long started = System.currentTimeMillis();

        for (;;) {
            // Do the lookup of the data source:
            DataSource ds = EjbUtils.findResource(ContainerManagedResourceType.DATA_SOURCE);
            if (ds != null) {
                return ds;
            }

            // Don't continue if we have already waited too long:
            long now = System.currentTimeMillis();
            long waited = now - started;
            if (waited > connectionTimeout) {
                log.error(
                        "The data source can't be located after waiting for more " +
                                "than {} seconds, giving up.", connectionTimeout / 1000
                );
                return null;
            }

            // It failed, so tell the user that the lookup failed but that we
            // will try again in a few seconds:
            log.warn(
                    "The data source can't be located after waiting for {} " +
                            "seconds, will try again.", waited / 1000
            );

            // Wait a bit before trying again:
            try {
                Thread.sleep(checkInterval);
            }
            catch (InterruptedException exception) {
                log.warn("Interrupted while waiting for data source, will try again: {}", exception.getMessage());
                log.debug("Exception", exception);
            }
        }
    }

    /**
     * Generate and sets the database engine dialect object according to configuration.
     *
     * @throws Exception
     */
    public static DbEngineDialect loadDbEngineDialect() {
        final String ENGINE_DB_ENGINE_PROPERTIES = "engine-db-engine.properties";
        final String DIALECT = "DbEngineDialect";
        Properties props = null;
        try {
            props = ResourceUtils.loadProperties(DbFacadeLocator.class, ENGINE_DB_ENGINE_PROPERTIES);
        }
        catch (IOException exception) {
            throw new IllegalStateException(
                "Can't load properties from resource \"" +
                ENGINE_DB_ENGINE_PROPERTIES + "\".", exception
            );
        }
        String dialect = props.getProperty(DIALECT);
        if (dialect == null) {
            throw new IllegalStateException(
                "Can't load property \"" + DIALECT + "\" from resource \"" +
                 ENGINE_DB_ENGINE_PROPERTIES + "\"."
            );
        }
        try {
            return (DbEngineDialect) Class.forName(dialect).newInstance();
        }
        catch (Exception exception) {
            throw new IllegalStateException(
                "Can't create instance of dialect class \"" + dialect + "\".",
                exception
            );
        }
    }

    public static void loadDbFacadeConfig() {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        try {
            connectionTimeout = config.getInteger("ENGINE_DB_CONNECTION_TIMEOUT");
            checkInterval = config.getInteger("ENGINE_DB_CHECK_INTERVAL");
        }
        catch (Exception exception) {
            log.warn("Can't load connection checking parameters of DB facade, "
                            + "will continue using the default values. Error: {}",
                exception.getMessage());
            log.debug("Exception", exception);
        }
    }

    public static void setDbFacade(DbFacade value) {
        dbFacade = value;
    }
}
