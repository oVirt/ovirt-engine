package org.ovirt.engine.core.dal.dbbroker;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCallOperations;

/**
 *
 * Database engine dialect interface. Handles all db engine specific issues like parameter prefix, search template etc.
 *
 */
public interface DbEngineDialect {

    /**
     * Create the {@link JdbcTemplate} to use in the {@link DbFacade} class.
     * TODO: Remove this method once we don't use SqlServer anymore, since it's a needed only for SqlServer
     * Compatibility.
     *
     * @param dataSource The Data Source to use for the template.
     * @return A {@link JdbcTemplate} that can be used to DB operations.
     */
    public JdbcTemplate createJdbcTemplate(DataSource dataSource);

    /**
     * Create the object used for performing function calls for querying data and returning a result/list of results.
     * This object can't be used safely to execute other commands, since it doesn't necessarily support features
     * such as out parameters. However, it must be used in order to query the DB correctly using a function call.
     *
     * @param jdbcTemplate The JDBC template is needed for actually performing the calls.
     * @return A {@link SimpleJdbcCallOperations} instance that can be used to call a query function in the DB.
     */
    public SimpleJdbcCallOperations createJdbcCallForQuery(JdbcTemplate jdbcTemplate);

    /**
     * Gets the engine prefix to be used for sp parameters.
     */
    public String getParamNamePrefix();

    /**
     * TODO: Remove this method once we don't use SqlServer anymore, since it's a needed only for SqlServer
     * Compatibility.
     * @return The default key of the returned value by a function when called using
     * {@link SimpleJdbcCallOperations#execute()}.
     */
    public String getFunctionReturnKey();

    /**
     * This function create a call SQL command for a specific procedure, for a specific database
     */
    public String createSqlCallCommand(String procSchemaFromDB,
            String procNameFromDB, String params);
}
