package org.ovirt.engine.core.dal.dbbroker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class SimpleJdbcCallsHandler {

    private ConcurrentMap<String, SimpleJdbcCall> callsMap =
            new ConcurrentHashMap<String, SimpleJdbcCall>();

    private DbEngineDialect dialect;

    private JdbcTemplate template;

    public void setDbEngineDialect(DbEngineDialect dialect) {
        this.dialect = dialect;
    }

    public void setJdbcTemplate(JdbcTemplate template) {
        this.template = template;
    }

    private interface CallCreator {
        SimpleJdbcCall createCall();
    }

    /**
     * Runs a set of stored procedure calls in a batch. Only useful for update procedures that return no value
     * @param procedureName the procedure name
     * @param executions a list of parameter maps
     * @return
     */
    public void executeStoredProcAsBatch(final String procName,
            final List<MapSqlParameterSource> executions)
            throws DataAccessException {

        template.execute(new BatchProcedureExecutionConnectionCallback(this, procName, executions));
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
        Map<String, Object> resultsMap = executeReadAndReturnMap(procedureName, mapper, parameterSource);
        List<?> resultList = (List<?>) (resultsMap
                .get(BaseDAODbFacade.RETURN_VALUE_PARAMETER));
        return (resultList != null) ? (List<T>) resultList : null;
    }

    public <T> Map<String, Object> executeReadAndReturnMap(final String procedureName,
            final RowMapper<T> mapper,
            final MapSqlParameterSource parameterSource) {
        Map<String, Object> resultsMap =
                executeImpl(procedureName, parameterSource, createCallForRead(procedureName, mapper, parameterSource));
        return resultsMap;
    }

    private CallCreator createCallForRead(final String procedureName,
            final RowMapper<?> mapper,
            final MapSqlParameterSource parameterSource) {
        return new CallCreator() {
                @Override
                public SimpleJdbcCall createCall() {
                    SimpleJdbcCall call =
                            (SimpleJdbcCall) dialect.createJdbcCallForQuery(template).withProcedureName(procedureName);
                    call.returningResultSet(BaseDAODbFacade.RETURN_VALUE_PARAMETER, mapper);
                    // Pass mapper information (only parameter names) in order to supply all the needed
                    // metadata information for compilation.
                    call.getInParameterNames().addAll(
                            SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource).keySet());
                    return call;
                }
            };
    }

    CallCreator createCallForModification(final String procedureName) {
        return new CallCreator() {
            @Override
            public SimpleJdbcCall createCall() {
                return new SimpleJdbcCall(template).withProcedureName(procedureName);
            }
        };
    }

    private Map<String, Object> executeImpl(String procedureName,
            MapSqlParameterSource paramsSource, CallCreator callCreator) {
        SimpleJdbcCall call = getCall(procedureName, callCreator);
        return call.execute(paramsSource);
    }

    /**
     * Creates a call object and compiles its metadata, if not found in the map.
     * Bare in mind the existence check if not atomic, so at worst case few more redundant information schema calls
     * * will be made.
     * The compilation is done at the scope of the method in order to avoid concurrency issues upon first time usage of
     * the stored procedure.
     * @param procedureName stored proceudre name
     * @param callCreator calls creator object
     * @return simple JDBC call object
     */
    protected SimpleJdbcCall getCall(String procedureName, CallCreator callCreator) {
        SimpleJdbcCall call = callsMap.get(procedureName);
        if (call == null) {
            call = callCreator.createCall();
            call.compile();
            callsMap.putIfAbsent(procedureName, call);
        }
        return call;
    }

    public DbEngineDialect getDialect() {
        return dialect;
    }
}
