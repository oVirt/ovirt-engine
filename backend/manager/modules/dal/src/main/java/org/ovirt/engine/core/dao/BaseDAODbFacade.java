package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
    protected RowMapper<Boolean> createBooleanMapper() {
        return new RowMapper<Boolean>() {

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
    protected RowMapper<Guid> createGuidMapper() {
        return new RowMapper<Guid>() {

            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Guid(rs.getString(1));
            }
        };
    }

    private static RowMapper<Long> longRowMapper = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong(1);
        };
    };

    protected RowMapper<Long> getLongMapper() {
        return longRowMapper;
    }

    private static RowMapper<Integer> integerRowMapper = new RowMapper<Integer>() {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt(1);
        };
    };

    protected RowMapper<Integer> getIntegerMapper() {
        return integerRowMapper;
    }

    protected SimpleJdbcCallsHandler getCallsHandler() {
        return dbFacade.getCallsHandler();
    }

    public void setDbFacade(DbFacade dbFacade) {
        this.dbFacade = dbFacade;
    }

    /**
     * Returns a Double or a null if the column was NULL.
     * @param resultSet the ResultSet to extract the result from
     * @param columnName
     * @return a Long or null
     * @throws SQLException
     */
    final static Long getLong(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getLong(columnName) == 0 && resultSet.wasNull()) {
            return null;
        } else {
            return resultSet.getLong(columnName);
        }
    }

}
