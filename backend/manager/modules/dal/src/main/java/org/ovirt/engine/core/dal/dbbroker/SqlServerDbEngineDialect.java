package org.ovirt.engine.core.dal.dbbroker;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcCallOperations;

/**
 *
 * SQL Server db engine dialect.
 *
 */
public class SqlServerDbEngineDialect implements DbEngineDialect {

    @Override
    public JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Override
    public SimpleJdbcCallOperations createJdbcCallForQuery(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate);
    }

    @Override
    public String getParamNamePrefix() {
        return "";
    }

    @Override
    public String getPreSearchQueryCommand() {
        return "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;";
    }

    @Override
    public String getFunctionReturnKey() {
        return "RETURN_VALUE";
    }
}
