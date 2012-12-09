package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public abstract class BaseDAODbFacade {

    protected JdbcTemplate jdbcTemplate;
    protected DbEngineDialect dialect;
    protected DbFacade dbFacade;

    public void setTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDialect(DbEngineDialect dialect) {
        this.dialect = dialect;
    }

    public static final String RETURN_VALUE_PARAMETER = "RETURN_VALUE";

    public BaseDAODbFacade() {
    }

    protected CustomMapSqlParameterSource getCustomMapSqlParameterSource() {
        return new CustomMapSqlParameterSource(dialect);
    }

    /**
     * @return A generic boolean result mapper which always takes the boolean result, regardless of how it's column is
     *         called.
     */
    protected ParameterizedRowMapper<Boolean> createBooleanMapper() {
        return new ParameterizedRowMapper<Boolean>() {

            @Override
            public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getBoolean(1);
            }
        };
    }

    /**
     * @return A generic {@link Guid} result mapper which always takes the {@link Guid} result, regardless of how it's
     *         column is called.
     */
    protected ParameterizedRowMapper<Guid> createGuidMapper() {
        return new ParameterizedRowMapper<Guid>() {

            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Guid(rs.getString(1));
            }
        };
    }

    private static ParameterizedRowMapper<Long> longRowMapper = new ParameterizedRowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong(1);
        };
    };

    protected ParameterizedRowMapper<Long> getLongMapper() {
        return longRowMapper;
    }

    protected SimpleJdbcCallsHandler getCallsHandler() {
        return dbFacade.getCallsHandler();
    }

    public void setDbFacade(DbFacade dbFacade) {
        this.dbFacade = dbFacade;
    }
}
