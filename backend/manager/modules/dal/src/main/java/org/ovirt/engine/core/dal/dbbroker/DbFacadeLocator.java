package org.ovirt.engine.core.dal.dbbroker;

import java.util.Properties;

import javax.sql.DataSource;

import org.ovirt.engine.core.utils.LocalConfig;
import org.ovirt.engine.core.utils.ResourceUtils;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

/**
 * A locator singleton for looking up (and initializing) DbFacade instance
 */
public class DbFacadeLocator {

    private static DbFacade dbFacade;
    private static final Log log = LogFactory.getLog(DbFacadeLocator.class);

    static {
        try {
            // ok we need to locate the datasource
            DataSource datasource = EjbUtils
                    .findResource(ContainerManagedResourceType.DATA_SOURCE);
            if (datasource == null)
                throw new RuntimeException("Datasource is not defined ");

            // create the facade and return it
            dbFacade = new DbFacade();
            dbFacade.setDbEngineDialect(loadDbEngineDialect());
            loadDbFacadeConfig();
            JdbcTemplate jdbcTemplate = dbFacade.getDbEngineDialect().createJdbcTemplate(datasource);
            SQLErrorCodeSQLExceptionTranslator tr = new CustomSQLErrorCodeSQLExceptionTranslator(datasource);
            jdbcTemplate.setExceptionTranslator(tr);

            dbFacade.setTemplate(jdbcTemplate);
        } catch (Exception e) {
            log.fatal("Unable to locate DbFacade instance", e);
        }
    }

    public static DbFacade getDbFacade() {
        return dbFacade;
    }

    /**
     * Generate and sets the database engine dialect object according to configuration.
     *
     * @throws Exception
     */
    public static DbEngineDialect loadDbEngineDialect() throws Exception {
        final String ENGINE_DB_ENGINE_PROPERTIES = "engine-db-engine.properties";
        final String DIALECT = "DbEngineDialect";
        Properties props = ResourceUtils.loadProperties(DbFacadeLocator.class, ENGINE_DB_ENGINE_PROPERTIES);
        if (props.getProperty(DIALECT) == null) {
            throw new IllegalStateException("Failed to get property key value");
        }

        return (DbEngineDialect) Class.forName(props.getProperty(DIALECT)).newInstance();
    }

    public static void loadDbFacadeConfig() throws Exception {
        int connectionTimeout = 300000;
        int checkInterval = 1000;
        LocalConfig config = LocalConfig.getInstance();
        try {
            connectionTimeout = config.getInteger("ENGINE_DB_CONNECTION_TIMEOUT");
            checkInterval = config.getInteger("ENGINE_DB_CHECK_INTERVAL");
        }
        catch (Exception exception) {
            log.error("Can't load connection checking parameters of DB facade.", exception);
        }
        dbFacade.setOnStartConnectionTimeout(connectionTimeout);
        dbFacade.setConnectionCheckInterval(checkInterval);
    }

    public static void setDbFacade(DbFacade value) {
        dbFacade = value;
    }
}
