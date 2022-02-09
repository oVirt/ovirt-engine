package org.ovirt.engine.core.dao;

import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public abstract class BaseDao {
    protected static final String SEPARATOR = ",";

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private DbEngineDialect dbEngineDialect;

    @Inject
    private SimpleJdbcCallsHandler callsHandler;

    public BaseDao() {
    }

    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    protected DbEngineDialect getDialect() {
        return dbEngineDialect;
    }

    protected CustomMapSqlParameterSource getCustomMapSqlParameterSource() {
        return new CustomMapSqlParameterSource(getDialect());
    }

    protected Array createArrayOf(String typeName, Object[] array) {
        try (Connection connection = getJdbcTemplate().getDataSource().getConnection()) {
            return connection.createArrayOf(typeName, array);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Array createArrayOfUUIDs(Collection<Guid> guids) {
        UUID[] uuids = guids.stream().map(Guid::getUuid).toArray(UUID[]::new);
        return createArrayOf("uuid", uuids);
    }

    /**
     * @return A generic {@link Guid} result mapper which always takes the {@link Guid} result, regardless of how it's
     *         column is called.
     */
    protected RowMapper<Guid> createGuidMapper() {
        return (rs, rowNum) -> new Guid((UUID) rs.getObject(1));
    }

    protected SimpleJdbcCallsHandler getCallsHandler() {
        return callsHandler;
    }

    /**
     * Returns a Double or a null if the column was NULL.
     * @param resultSet the ResultSet to extract the result from
     * @return a Double or null
     */
    public static Double getDouble(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getDouble(columnName) == 0 && resultSet.wasNull()) {
            return null;
        } else {
            return resultSet.getDouble(columnName);
        }
    }

    /**
     * Returns a Long or a null if the column was NULL.
     * @param resultSet the ResultSet to extract the result from
     * @return a Long or null
     */
    public static Long getLong(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getLong(columnName) == 0 && resultSet.wasNull()) {
            return null;
        } else {
            return resultSet.getLong(columnName);
        }
    }

    /**
     * Returns a BigDecimal or a null if the column was NULL.
     * @param resultSet the ResultSet to extract the result from
     * @return a BigDecimal or null
     */
    public static BigInteger getBigInteger(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBigDecimal(columnName) == null ? null : resultSet.getBigDecimal(columnName).toBigIntegerExact();
    }

    /**
     * Returns a Integer or a null if the column was NULL.
     * @param resultSet the ResultSet to extract the result from
     * @return a Integer or null
     */
    protected static Integer getInteger(ResultSet resultSet, String columnName) throws SQLException {
        Integer value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    /**
     * Returns a {@link Guid} representing the column's value or a default value if the column was {@code null}.
     *
     * <b>Note:</b> Postgres' driver returns a {@link UUID} when {@link ResultSet#getObject(String)} is called on a
     * uuid column. This behavior is trusted to work with Postgres 8.x and above and is used to achieve the best
     * performance. If it is ever broken on Postgres' side, this method should be rewritten.
     *
     * @param resultSet the ResultSet to extract the result from.
     * @param columnName the name of the column.
     * @param defaultValue The value to return if the column is {@code null}.
     * @return a {@link Guid} representing the UUID in the column, or the default value if it was {@code null}.
     * @throws SQLException If resultSet does not contain columnName or its value cannot be cast to {@link UUID}.
     */
    private static Guid getGuid(ResultSet resultSet, String columnName, Guid defaultValue) throws SQLException {
        UUID uuid = (UUID) resultSet.getObject(columnName);
        if (uuid == null || resultSet.wasNull()) {
            return defaultValue;
        }
        return new Guid(uuid);
    }

    /**
     * Returns a {@link Guid} representing the column's value or a {@code null}
     * if the column was {@code null}.
     *
     * @param resultSet the ResultSet to extract the result from.
     * @param columnName the name of the column.
     * @return a {@link Guid} representing the UUID in the column, or the default value if it was {@code null}.
     * @throws SQLException If resultSet does not contain columnName or its value cannot be cast to {@link UUID}.
     */
    protected static Guid getGuid(ResultSet resultSet, String columnName) throws SQLException {
        return getGuid(resultSet, columnName, null);
    }

    /**
     * Returns a {@link Guid} representing the column's value or a {@link Guid#Empty}
     * if the column was {@code null}.
     *
     * @param resultSet the ResultSet to extract the result from.
     * @param columnName the name of the column.
     * @return a {@link Guid} representing the UUID in the column, or the default value if it was {@code null}.
     * @throws SQLException If resultSet does not contain columnName or its value cannot be cast to {@link UUID}.
     */
    protected static Guid getGuidDefaultEmpty(ResultSet resultSet, String columnName) throws SQLException {
        return getGuid(resultSet, columnName, Guid.Empty);
    }

    /**
     * Returns a {@link Guid} representing the column's value or a {@link Guid#newGuid()}
     * if the column was {@code null}.
     *
     * @param resultSet  the ResultSet to extract the result from.
     * @param columnName the name of the column.
     * @return a {@link Guid} representing the UUID in the column, or the default value if it was {@code null}.
     * @throws SQLException If resultSet does not contain columnName or its value cannot be cast to {@link UUID}.
     */
    protected static Guid getGuidDefaultNewGuid(ResultSet resultSet, String columnName) throws SQLException {
        return getGuid(resultSet, columnName, Guid.newGuid());
    }

    protected static ArrayList<String> split(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        return new ArrayList<>(Arrays.asList(str.split(SEPARATOR)));
    }
}
