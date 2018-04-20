package org.ovirt.engine.core.dal.dbbroker;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * A locator singleton for looking up (and initializing) DbFacade instance
 */
@Singleton
public class DbFacadeLocator {
    private static final Logger log = LoggerFactory.getLogger(DbFacadeLocator.class);

    // Default values for the configuration (these will be replaced with the
    // values from the configuration file):
    private static final int DEFAULT_CHECK_INTERVAL = 5000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    // Time to wait between checks of the database connection and maximum time
    // to wait for a connection:
    private int checkInterval = DEFAULT_CHECK_INTERVAL;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    @Resource(mappedName = "java:/ENGINEDataSource")
    @Produces
    private DataSource dataSource;

    @Produces
    @Singleton
    public JdbcTemplate produceJdbcTemplate(
            DataSource dataSource,
            DbEngineDialect dbEngineDialect,
            SQLExceptionTranslator sqlExceptionTranslator) {
        final JdbcTemplate jdbcTemplate = dbEngineDialect.createJdbcTemplate(dataSource);
        jdbcTemplate.setExceptionTranslator(sqlExceptionTranslator);
        return jdbcTemplate;
    }

    @Produces
    @Singleton
    public DbEngineDialect produceDbEngineDialect() {
        return loadDbEngineDialect();
    }

    @Produces
    @Singleton
    public DbConnectionUtil produceDbConnectionUtil(JdbcTemplate jdbcTemplate) {
        loadDbFacadeConfig();
        return new DbConnectionUtil(jdbcTemplate, connectionTimeout, checkInterval);
    }

    DbFacadeLocator() {
    }

    /**
     * Generate and sets the database engine dialect object according to configuration.
     *
     * @throws IllegalStateException If one of the expected properties is not present.
     */
    public static DbEngineDialect loadDbEngineDialect() {
        final String ENGINE_DB_ENGINE_PROPERTIES = "engine-db-engine.properties";
        final String DIALECT = "DbEngineDialect";
        final Properties props;
        try {
            props = ResourceUtils.loadProperties(DbFacadeLocator.class, ENGINE_DB_ENGINE_PROPERTIES);
        } catch (IOException exception) {
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
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Can't create instance of dialect class \"" + dialect + "\".",
                exception
            );
        }
    }

    private void loadDbFacadeConfig() {
        final EngineLocalConfig config = EngineLocalConfig.getInstance();
        try {
            connectionTimeout = config.getInteger("ENGINE_DB_CONNECTION_TIMEOUT");
            checkInterval = config.getInteger("ENGINE_DB_CHECK_INTERVAL");
        } catch (Exception exception) {
            log.warn("Can't load connection checking parameters of DB facade, "
                            + "will continue using the default values. Error: {}",
                exception.getMessage());
            log.debug("Exception", exception);
        }
    }
}
