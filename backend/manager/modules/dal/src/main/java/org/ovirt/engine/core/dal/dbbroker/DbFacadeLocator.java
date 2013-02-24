package org.ovirt.engine.core.dal.dbbroker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.utils.FileUtil;
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
        boolean configSucceeded = false;
        final String ENGINE_CONF_FILE = "/etc/ovirt-engine/engine.conf";
        final String ON_START_CONNECTION_TIMEOUT = "OnStartConnectionTimeout";
        final String CONNECTION_CHECK_INTERVAL = "ConnectionCheckInterval";
        final String DEFAULT_TIMEOUT_VALUE = "300000";
        final String DEFAULT_INTERVAL_VALUE = "5000";
        InputStream inputStream = null;
        try {
            String onStartConnectionTimeout = null;
            String connectionCheckInterval = null;
            Properties props = new Properties();
            if (FileUtil.fileExists(ENGINE_CONF_FILE)) {
                // File exists, load /etc/ovirt-engine/engine.conf and set values in DbFacade
                inputStream = new FileInputStream(ENGINE_CONF_FILE);
                props.load(inputStream);
                onStartConnectionTimeout = props.getProperty(ON_START_CONNECTION_TIMEOUT);
                connectionCheckInterval = props.getProperty(CONNECTION_CHECK_INTERVAL);
                if (!validNumber(onStartConnectionTimeout)) {
                    onStartConnectionTimeout = DEFAULT_TIMEOUT_VALUE;
                }
                if (!validNumber(connectionCheckInterval)) {
                    connectionCheckInterval = DEFAULT_INTERVAL_VALUE;
                }
            } else {
                // File does not exist - use defaults
                log.warn(String.format("%1$s file is not found. Please check your engine installation. " +
                        "Default values will be used"
                        , ENGINE_CONF_FILE));
                onStartConnectionTimeout = DEFAULT_TIMEOUT_VALUE;
                connectionCheckInterval = DEFAULT_INTERVAL_VALUE;
            }
            dbFacade.setOnStartConnectionTimeout(Integer.parseInt(onStartConnectionTimeout));
            dbFacade.setConnectionCheckInterval(Integer.parseInt(connectionCheckInterval));
            configSucceeded = true;
        } catch (Exception ex) {
            log.error("Error in configuration of db facade " + ExceptionUtils.getMessage(ex));
        } finally {
            if (!configSucceeded) {
                dbFacade.setOnStartConnectionTimeout(300000);
                dbFacade.setConnectionCheckInterval(1000);
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

    private static boolean validNumber(String numberStr) {
        return numberStr != null && NumberUtils.isNumber(numberStr);
    }

    public static void setDbFacade(DbFacade value) {
        dbFacade = value;
    }
}
