package org.ovirt.engine.core.dao;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * <code>BaseDaoTestCase</code> provides a foundation for creating unit tests for the persistence layer. The annotation
 * <code>@Transactional</code>, and the listener <code>TransactionalTestExecutionListener</code> ensure that all test
 * cases (<code>@Test</code> methods) are executed inside a transaction, and the transaction is automatically rolled
 * back on completion of the test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ TransactionalTestExecutionListener.class, DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:/test-beans.xml" })
@Transactional
public abstract class BaseDaoTestCase {
    protected static final Guid PRIVILEGED_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    protected static final String PRIVILEGED_USER_ENGINE_SESSION_ID = "c6f975b2-6f67-11e4-8455-3c970e14c386";
    protected static final Guid UNPRIVILEGED_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
    protected static final String UNPRIVILEGED_USER_ENGINE_SESSION_ID = "9ee57fd0-6f67-11e4-9e67-3c970e14c386";
    private static boolean initialized = false;

    @Inject
    protected DbFacade dbFacade;
    private static Object dataFactory;
    protected static boolean needInitializationSql = false;
    protected static String initSql;
    protected static DataSource dataSource;

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    @BeforeClass
    public static void initTestCase() throws Exception {
        if(dataSource == null) {
            try {
                dataSource = createDataSource();
                ejbRule.mockResource(ContainerManagedResourceType.DATA_SOURCE, dataSource);

                final IDataSet dataset = initDataSet();
                // load data from fixtures to DB
                DatabaseOperation.CLEAN_INSERT.execute(getConnection(), dataset);
                SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
                builder.bind("java:/ENGINEDataSource", dataSource);
                builder.activate();
                initialized = true;
            } catch (Exception e) {
                /*
                 * note: without logging current maven setting does NOT produce stacktrace/message for following AssertionError.
                 * this error log is absolutely vital to actually see, what went wrong!
                 */
                LoggerFactory.getLogger(BaseDaoTestCase.class).error("Unable to init tests", e);

                /*
                 * note: re-throwing exception here is absolutely vital. Without it, all tests of first executed
                 * descendant test class will be normally executed. With added assumption using Assume then all tests
                 * will be skipped and successful tests execution will be pronounced. This exception will cause first of
                 * executed descendant test class fail and it's constructor will not be even reached.
                 */
                throw new AssertionError("Unable to init tests", e);
            }
        }
    }

    public BaseDaoTestCase() {
        /*
         * note: all tests, which reached this point when initialization failed, can be skipped, but only if first
         * executed test class stated failure. Otherwise all tests will be skipped and success pronounced.
         */
        assumeTrue("Uninitialized TestCase, cannot proceed. Look above for causing exception.", initialized);
    }

    @Before
    public void setUp() throws Exception {
    }

    protected static IDataSet initDataSet() throws Exception {
        return new XmlDataSet(BaseDaoTestCase.class.getResourceAsStream(
                "/fixtures.xml"));
    }

    protected static IDatabaseConnection getConnection() throws Exception {
        // get connection and setup it's meta data
        Connection con = dataSource.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(con);
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataFactory);
        if (needInitializationSql) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(initSql);
            }
        }
        return connection;
    }

    private static DataSource createDataSource() {
        DataSource result = null;
        Properties properties = new Properties();

        Config.setConfigUtils(new DBConfigUtils(false));

        InputStream is = null;
        try {
            String job = System.getProperty("JOB_NAME");
            if (job == null) {
                job = "";
            }
            String number = System.getProperty("BUILD_NUMBER");
            if (number == null) {
                number = "";
            }
            String schemaNamePostfix = job + number;
            is = BaseDaoTestCase.class.getResourceAsStream(
                    "/test-database.properties");
            properties.load(is);

            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            String dbUrl = properties.getProperty("database.url") + schemaNamePostfix;
            result = new SingleConnectionDataSource(
                    dbUrl,
                    properties.getProperty("database.username"),
                    properties.getProperty("database.password"), true);

            initSql = properties.getProperty("database.initsql");

            loadDataFactory(properties.getProperty("database.testing.datafactory"));

            if (initSql != null && !initSql.isEmpty()) {
                needInitializationSql = true;
            }
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("Cannot create data source", error);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return result;
    }

    private static void loadDataFactory(String dataFactoryClassname) throws Exception {
        Class<?> clazz = Class.forName(dataFactoryClassname);
        dataFactory = clazz.newInstance();
    }

    public DbFacade getDbFacade() {
        return dbFacade;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
