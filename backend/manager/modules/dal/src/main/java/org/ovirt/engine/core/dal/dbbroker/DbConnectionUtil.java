package org.ovirt.engine.core.dal.dbbroker;

import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class DbConnectionUtil {
    private final JdbcTemplate jdbcTemplate;

    private final int onStartConnectionTimeout;
    private final int connectionCheckInterval;

    public DbConnectionUtil(
            JdbcTemplate jdbcTemplate,
            int onStartConnectionTimeout,
            int connectionCheckInterval) {

        Objects.requireNonNull(jdbcTemplate);

        this.onStartConnectionTimeout = onStartConnectionTimeout;
        this.connectionCheckInterval = connectionCheckInterval;

        this.jdbcTemplate = jdbcTemplate;
    }

    /***
     * CheckDBConnection calls a simple "select 1" SP to verify DB is up & running.
     *
     * @return True if DB is up & running.
     */
    public boolean checkDBConnection() {
        return new SimpleJdbcCall(jdbcTemplate).withProcedureName("CheckDBConnection").execute() != null;
    }

    public int getOnStartConnectionTimeout() {
        return onStartConnectionTimeout;
    }

    public int getConnectionCheckInterval() {
        return connectionCheckInterval;
    }
}
