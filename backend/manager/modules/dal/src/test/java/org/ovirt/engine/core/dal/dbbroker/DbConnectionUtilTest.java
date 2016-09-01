package org.ovirt.engine.core.dal.dbbroker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.DirtiesContext;

public class DbConnectionUtilTest extends BaseDaoTestCase{

    @Inject
    private JdbcTemplate jdbcTemplate;

    private DbConnectionUtil underTest;

    @Before
    public void setUp() {
    }

    /**
     * Ensures that the checkDBConnection method returns true when the connection is up
     */
    @Test
    public void testDBConnectionWithConnection() {
        underTest = new DbConnectionUtil(jdbcTemplate, 666, 777);
        assertTrue(underTest.checkDBConnection());
    }

    /**
     * Ensures that the checkDBConnection method throws an Exception when connection is not valid
     */
    @Test
    @DirtiesContext
    public void testDBConnectionWithoutConnection() {

        Config.setConfigUtils(new DBConfigUtils(false));
        try (InputStream is = super.getClass().getResourceAsStream("/test-database.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            DataSource dataSource = new SingleConnectionDataSource(
                    properties.getProperty("database.url"),
                    // Deliberately puts a none existing user name, so an
                    // exception will be thrown when trying to check the
                    // connection
                    "no-such-username",
                    properties.getProperty("database.password"),
                    true);
            final DbEngineDialect dbEngineDialect = DbFacadeLocator.loadDbEngineDialect();
            final JdbcTemplate jdbcTemplate = dbEngineDialect.createJdbcTemplate(dataSource);
            underTest = new DbConnectionUtil(jdbcTemplate, 666, 777);

            underTest.checkDBConnection();

            fail("Connection should be down since the DataSource has an invalid username");
            // If DataAccessException is thrown - the test has succeeded. Was unable to do
            // with "expected" annotation, presumably since we are using DbUnit
        } catch (DataAccessException desiredException) {
            assertTrue(true);
            // If this exception is thrown we fail the test
        } catch (Exception undesiredException) {
            undesiredException.printStackTrace();
            fail();
        }
    }
}
