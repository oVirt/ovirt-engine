package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for testing database migration scripts.
 * Provides infrastructure to execute SQL migration scripts against the test database.
 * <p>
 * Subclasses should:
 * <ol>
 *   <li>Add pre-migration fixture data to {@code fixtures.xml}</li>
 *   <li>Implement {@link #getMigrationScriptName()} and call {@link #executeMigrationScript()} in test methods</li>
 *   <li>Verify post-migration state using DAOs or direct SQL queries</li>
 * </ol>
 * <p>
 * Tests run in transactions that are rolled back, so fixture modifications don't persist.
 *
 * @param <D> DAO type (unused, but required by BaseDaoTestCase)
 */
public abstract class BaseMigrationScriptTest<D extends Dao> extends BaseDaoTestCase<D> {
    private static final Logger log = LoggerFactory.getLogger(BaseMigrationScriptTest.class);

    @BeforeEach
    public void verifyPreconditions() {
        assertNotNull(BaseDaoTestCase.getDataSource(), "DataSource must be initialized");
    }

    /**
     * The filename of the migration script this test class exercises (e.g.,
     * "04_05_0370_migrate_i440fx_vms_and_cluster_defaults.sql").
     */
    protected abstract String getMigrationScriptName();

    /**
     * Executes the migration script returned by {@link #getMigrationScriptName()}
     * from the packaging/dbscripts/upgrade directory.
     *
     * @throws IOException if the script file cannot be read
     * @throws SQLException if the script execution fails
     */
    protected void executeMigrationScript() throws IOException, SQLException {
        executeMigrationScript(getMigrationScriptName());
    }

    /**
     * Executes a migration script by name from the packaging/dbscripts/upgrade directory.
     *
     * @param scriptName the filename of the migration script
     * @throws IOException if the script file cannot be read
     * @throws SQLException if the script execution fails
     */
    protected void executeMigrationScript(String scriptName) throws IOException, SQLException {
        // Tests run with the module directory as working directory; the packaging
        // directory lives at the repository root (4 levels up from the dal module).
        Path scriptPath = Paths.get("..", "..", "..", "..", "packaging", "dbscripts", "upgrade", scriptName)
                .toAbsolutePath()
                .normalize();
        log.info("Executing migration script: {}", scriptPath.toAbsolutePath());

        String scriptContent = Files.readString(scriptPath);

        try (Connection con = BaseDaoTestCase.getDataSource().getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(scriptContent);
            log.info("Migration script executed successfully");
        }
    }

    /**
     * Executes a raw SQL statement against the test database.
     * Useful for setup or verification queries.
     *
     * @param sql the SQL statement to execute
     * @throws SQLException if execution fails
     */
    protected void executeSql(String sql) throws SQLException {
        try (Connection con = BaseDaoTestCase.getDataSource().getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        }
    }
}
