package org.ovirt.engine.core.dal.dbbroker;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcCallOperations;

/**
 *
 * Postgres db engine dialect.
 *
 */
public class PostgresDbEngineDialect implements DbEngineDialect {

    /**
     * Prefix used in our PostgreSQL function parameters.
     */
    private static final String PREFIX = "v_";

    /**
     * A {@link JdbcTemplate} extension for PostgreSQL DB, for stripping the 'v_' prefix from parameters returned by
     * function calls.
     */
    public static class PostgresJdbcTemplate extends JdbcTemplate {

        /**
         * @see JdbcTemplate#JdbcTemplate(DataSource)
         */
        public PostgresJdbcTemplate(DataSource dataSource) {
            super(dataSource);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Map extractOutputParameters(CallableStatement cs, List parameters) throws SQLException {
            Map<String, Object> outputParameters = super.extractOutputParameters(cs, parameters);
            Map<String, Object> returnMap = new HashMap<>(outputParameters.size());

            for (Map.Entry<String, Object> outputEntry : outputParameters.entrySet()) {
                // Get the value before we change the key (otherwise we won't be able to get it later when we need it).
                String parameter = outputEntry.getKey();
                Object value = outputEntry.getValue();
                if (parameter != null && parameter.length() > PREFIX.length() && parameter.startsWith(PREFIX)) {
                    parameter = parameter.substring(PREFIX.length());
                }

                returnMap.put(parameter, value);
            }

            return returnMap;
        }
    }

    /**
     * This class is needed in order to call the functions in PostgreSQL correctly. Since the functions in PostgreSQL
     * which use REFCURSORS to return a set of rows need to run in a connection with autoCommit = false, it poses a
     * problem in the existing codebase, since some of the operations which occur in the code occur outside an active
     * transaction and running them in auto-commit false leaves the transactions which they participated in running in
     * the DB (causing deadlocks and other sporadic behavior).<br>
     * <br>
     * PostgreSQL offers a better way to work with functions which return a set of rows, called SETOF:
     * http://wiki.postgresql.org/wiki/Return_more_than_one_row_of_data_from_PL/pgSQL_functions<br>
     * <br>
     * In order to use this functionality, we need to call the function as a table: select * from function()<br>
     * instead of the usual stored procedure call: {call function()}<br>
     * It is so because the values returned are treated by PostgreSQL as a table.<br>
     * <br>
     * The {@link SimpleJdbcCall} can't handle this, so we need a few hacks to make it work.
     */
    private static class PostgresSimpleJdbcCall extends SimpleJdbcCall {

        /**
         * This is the key to put in the returned map, emulating the way the {@link SimpleJdbcCallOperations} works.
         */
        private String returnedMapKey;

        /**
         * Row mapper is used to map the Result Set to POJOs.
         */
        private RowMapper rowMapper;

        public PostgresSimpleJdbcCall(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        @Override
        protected void compileInternal() {
            // Put a dummy parameter name as an input parameters, otherwise the CallMetaDataContext thinks that the
            // returned column names are parameters.
            Set<String> inParams = new HashSet<>(getInParameterNames());
            inParams.add("dummyParamNeverUsed");
            setInParameterNames(inParams);
            super.compileInternal();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Map<String, Object> doExecute(SqlParameterSource parameterSource) {
            // Have only the declared parameters participate in the function metadata extraction, otherwise the
            // CallMetaDataContext thinks that the returned column names are parameters.
            getInParameterNames().addAll(
                    SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource).keySet());
            checkCompiled();
            Map params = matchInParameterValuesWithCallParameters(parameterSource);
            return executeCallInternal(params);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Map<String, Object> doExecute(Map<String, ?> args) {
            // Have only the declared parameters participate in the function metadata extraction, otherwise the
            // CallMetaDataContext thinks that the returned column names are parameters.
            getInParameterNames().addAll(args.keySet());
            checkCompiled();
            Map params = matchInParameterValuesWithCallParameters(args);
            return executeCallInternal(params);
        }

        /**
         * Save the row mapper and parameter name locally to use later in the execution.
         */
        @SuppressWarnings("rawtypes")
        @Override
        public SimpleJdbcCall returningResultSet(String parameterName, RowMapper rowMapper) {
            this.returnedMapKey = parameterName;
            this.rowMapper = rowMapper;
            return this;
        }

        /**
         * Execute the call using a query instead of a procedure call.<br>
         * <br>
         * The way to execute correctly is to use a {@link  org.springframework.jdbc.core.PreparedStatementSetter} which
         * will set the parameters correctly, since using a PreparedStatementCreator doesn't seem to work well. The
         * setter simply sets the parameter using the correct {@link Types} constant indicating the actual call type.
         */
        private Map<String, Object> executeCallInternal(final Map<String, Object> params) {
            Map<String, Object> result = new HashMap<>(1);
            result.put(returnedMapKey, getJdbcTemplate().query(
                    generateSql(),
                    ps -> {
                        List<SqlParameter> callParameters = getCallParameters();
                        for (int i = 0; i < callParameters.size(); i++) {
                            SqlParameter parameter = callParameters.get(i);
                            ps.setObject(i + 1, params.get(parameter.getName()), parameter.getSqlType());
                        }
                    }, rowMapper));
            return result;
        }

        /**
         * @return The query used for calling the function.
         */
        private String generateSql() {
            StringBuilder builder = new StringBuilder("select * from ");
            builder.append(getCallString().replace("{call", "").replace("}", ""));
            return builder.toString();
        }
    }

    @Override
    public JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        return new PostgresJdbcTemplate(dataSource);
    }

    @Override
    public SimpleJdbcCallOperations createJdbcCallForQuery(JdbcTemplate jdbcTemplate) {
        return new PostgresSimpleJdbcCall(jdbcTemplate);
    }

    @Override
    public String getParamNamePrefix() {
        return PREFIX;
    }

    @Override
    public String getFunctionReturnKey() {
        return "returnvalue";
    }

    @Override
    public String createSqlCallCommand(String procSchemaFromDB,
            String procNameFromDB, String params) {
        StringBuilder sqlCommand = new StringBuilder();
        sqlCommand.append("{call ").append(procSchemaFromDB).append(".")
                .append(procNameFromDB).append("(").append(params).append(")}");
        return sqlCommand.toString();
    }
}
