package org.ovirt.engine.core.dal.dbbroker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Singleton
public class SimpleJdbcCallsHandler {

    private static final String RETURN_VALUE_PARAMETER = "RETURN_VALUE";

    private final ConcurrentMap<String, SimpleJdbcCall> callsMap = new ConcurrentHashMap<>();

    private final DbEngineDialect dialect;
    private final JdbcTemplate jdbcTemplate;

    @Inject
    public SimpleJdbcCallsHandler(
            DbEngineDialect dialect,
            JdbcTemplate jdbcTemplate) {
        Objects.requireNonNull(dialect, "DbEngineDialect cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");

        this.dialect = dialect;
        this.jdbcTemplate = jdbcTemplate;
    }

    private interface CallCreator {
        SimpleJdbcCall createCall();
    }

    /**
     * Runs a set of stored procedure calls in a batch. Only useful for update procedures that return no value
     *
     * @param procName
     *            the procedure name
     * @param executions
     *            a list of parameter maps
     */
    public void executeStoredProcAsBatch(final String procName,
            final List<MapSqlParameterSource> executions)
            throws DataAccessException {

        jdbcTemplate.execute(new BatchProcedureExecutionConnectionCallback(this, procName, executions));
    }

    /**
     * Runs a set of stored procedure calls in a batch. Only useful for update procedures that return no value
     * @param procedureName the procedure name
     * @param paramValues list of objects to be converted to {@link MapSqlParameterSource}
     * @param mapper mapper to use to convert the param value objects to {@link MapSqlParameterSource}
     */
    public <T> void executeStoredProcAsBatch(final String procedureName,
            Collection<T> paramValues,
            MapSqlParameterMapper<T> mapper) {
        List<MapSqlParameterSource> sqlParams = paramValues.stream().map(mapper::map).collect(Collectors.toList());

        executeStoredProcAsBatch(procedureName, sqlParams);
    }

    public Map<String, Object> executeModification(final String procedureName, final MapSqlParameterSource paramSource) {
        return executeImpl(procedureName, paramSource, createCallForModification(procedureName));
    }

    public int executeModificationReturnResult(final String procedureName, final MapSqlParameterSource paramSource) {
        Integer procedureResult = null;
        Map<String, Object> result = executeImpl(procedureName, paramSource, createCallForModification(procedureName));
        if (!result.isEmpty()) {
            List<?> resultArray = (List<?>) result.values().iterator().next();
            if (resultArray != null && !resultArray.isEmpty()) {
                Map<?, ?> resultMap = (Map<?, ?>) resultArray.get(0);
                if (!resultMap.isEmpty()) {
                    procedureResult = (Integer) resultMap.values().iterator().next();
                }
            }
        }
        return (procedureResult != null) ? procedureResult : 0;
    }

    public <T> T executeRead(final String procedureName,
            final RowMapper<T> mapper,
            final MapSqlParameterSource parameterSource) {
        List<T> results = executeReadList(procedureName, mapper, parameterSource);
        return results.isEmpty() ? null : results.get(0);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> executeReadList(final String procedureName,
            final RowMapper<T> mapper,
            final MapSqlParameterSource parameterSource) {
        Map<String, Object> resultsMap = executeImpl(procedureName, parameterSource, createCallForRead(procedureName, mapper, parameterSource), mapper);
        return (List<T>) resultsMap.get(RETURN_VALUE_PARAMETER);
    }

    private CallCreator createCallForRead(final String procedureName,
            final RowMapper<?> mapper,
            final MapSqlParameterSource parameterSource) {
        return () -> {
            SimpleJdbcCall call =
                    (SimpleJdbcCall) dialect.createJdbcCallForQuery(jdbcTemplate).withProcedureName(procedureName);
            call.returningResultSet(RETURN_VALUE_PARAMETER, mapper);
            // Pass mapper information (only parameter names) in order to supply all the needed
            // metadata information for compilation.
            call.getInParameterNames().addAll(
                    SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource).keySet());
            return call;
        };
    }

    CallCreator createCallForModification(final String procedureName) {
        return () -> new SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName);
    }

    private Map<String, Object> executeImpl(String procedureName,
                                                MapSqlParameterSource paramsSource, CallCreator callCreatorr) {
        return executeImpl(procedureName, paramsSource, callCreatorr, null);
    }
    private <T> Map<String, Object> executeImpl(String procedureName,
            MapSqlParameterSource paramsSource, CallCreator callCreator, RowMapper<T> mapper) {
        SimpleJdbcCall call = getCall(procedureName, callCreator, mapper);
        return call.execute(paramsSource);
    }

    /**
     * Creates a call object and compiles its metadata, if not found in the map. Bare in mind the existence check if not
     * atomic, so at worst case few more redundant information schema calls * will be made. The compilation is done at
     * the scope of the method in order to avoid concurrency issues upon first time usage of the stored procedure.
     *
     * @param procedureName
     *            stored procedure name
     * @param callCreator
     *            calls creator object
     * @return simple JDBC call object
     */
    protected SimpleJdbcCall getCall(String procedureName, CallCreator callCreator) {
        return getCall(procedureName, callCreator, null);
    }

    protected <T> SimpleJdbcCall getCall(String procedureName, CallCreator callCreator, RowMapper<T> mapper) {
        SimpleJdbcCall call = callsMap.get(procedureName);
        if (call == null) {
            call = callCreator.createCall();
            call.compile();
            callsMap.putIfAbsent(procedureName, call);
        } else if (mapper != null) {
            call.returningResultSet(RETURN_VALUE_PARAMETER, mapper);
        }
        return call;
    }

    public DbEngineDialect getDialect() {
        return dialect;
    }
}
