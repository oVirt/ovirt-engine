package org.ovirt.engine.core.dal.dbbroker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public final class BatchProcedureExecutionConnectionCallback implements ConnectionCallback<Object> {
    private static final Logger log = LoggerFactory.getLogger(BatchProcedureExecutionConnectionCallback.class);
    private static ConcurrentMap<String, StoredProcedureMetaData> storedProceduresMap =
            new ConcurrentHashMap<>();

    private final String procName;
    private final List<MapSqlParameterSource> executions;
    private final SimpleJdbcCallsHandler handler;
    private final DbEngineDialect dbEngineDialect;

    public BatchProcedureExecutionConnectionCallback(SimpleJdbcCallsHandler handler,
            String procName,
            List<MapSqlParameterSource> executions) {
        this.handler = handler;
        this.procName = procName;
        this.executions = executions;
        this.dbEngineDialect = handler.getDialect();
    }

    @Override
    public Object doInConnection(Connection con) throws SQLException,
            DataAccessException {
        log.debug("Executing batch for procedure " + procName);
        StoredProcedureMetaData procMetaData = getStoredProcedureMetaData(
                procName, con);

        try (CallableStatement stmt = con.prepareCall(procMetaData.getSqlCommand())) {

            for (MapSqlParameterSource execution : executions) {
                mapParams(stmt, execution,
                        procMetaData.getParamatersMetaData());
                stmt.addBatch();
            }

            stmt.executeBatch();
            log.debug("Executed batch");
        } catch (SQLException e) {
            log.error("Can't execute batch: {}", e.getMessage());
            log.debug("Exception", e);

            if (e.getNextException() != null) {
                log.error("Can't execute batch. Next exception is: {}",
                        e.getNextException().getMessage());
                log.debug("Exception", e.getNextException());
            }
            throw e;
        }

        return null;
    }

    private StoredProcedureMetaData getStoredProcedureMetaData(
            String procName, Connection con) throws SQLException {

        if (!storedProceduresMap.containsKey(procName)) {
            StoredProcedureMetaData procMetaData = new StoredProcedureMetaData();
            fillProcMetaData(procName, con, procMetaData);
            storedProceduresMap.putIfAbsent(procName, procMetaData);
        }
        return storedProceduresMap.get(procName);
    }

    private void fillProcMetaData(String procName, Connection con,
            StoredProcedureMetaData procMetaData) throws SQLException {
        SimpleJdbcCall call = handler.getCall(procName,
                handler.createCallForModification(procName));

        Map<String, SqlCallParameter> paramOrder = new HashMap<>();
        String procNameFromDB = null;
        String procSchemaFromDB = null;
        StringBuilder params = new StringBuilder();
        try (ResultSet rs2 = con.getMetaData().getProcedureColumns(null,
                null, call.getProcedureName().toLowerCase(), "%")) {


            int internalCounter = 1;

            while (rs2.next()) {
                ProcData procData = fillProcData(rs2, internalCounter);
                if (procData != null) {
                    ++internalCounter;
                    paramOrder.put(procData.getColName(), new SqlCallParameter(
                            procData.getOrdinal(), procData.getColName(), procData.getDataType()));
                    procNameFromDB = procData.getProcName();
                    procSchemaFromDB = procData.getSchemaName();
                    params.append("CAST (? AS ").append(procData.getDbTypeName())
                            .append("),");
                }
            }

            if (params.length() > 0) {
                params.deleteCharAt(params.length() - 1);
            }
        } catch (SQLException e) {
            log.error("Can't get procedure '{}' meta data: {}", procName, e.getMessage());
            log.debug("Exception", e);
        }

        procMetaData.setSqlCommand(handler.getDialect().createSqlCallCommand(
                procSchemaFromDB, procNameFromDB, params.toString()));
        procMetaData.setDbName(procNameFromDB);
        procMetaData.setParamatersMetaData(paramOrder);
        procMetaData.setSchemaName(procSchemaFromDB);
    }

    private ProcData fillProcData(ResultSet rs, int internalCounter) throws SQLException {
        String colName = rs.getString("COLUMN_NAME");

        if (colName.equalsIgnoreCase("returnValue")) {
            return null;
        }
        ProcData retValue = new ProcData();

        retValue.setColName(colName);

        try {
            retValue.setOrdinal(rs.getInt("ORDINAL_POSITION"));
        } catch (SQLException e) {
            // TODO: Delete when moving to Postgres Driver 9.1
            // For some reason, some postgres drivers don't
            // provide ORDINAL_POSITION

            retValue.setOrdinal(internalCounter);
        }
        retValue.setDataType(rs.getInt("DATA_TYPE"));
        retValue.setDbTypeName(rs.getString("TYPE_NAME"));
        retValue.setProcName(rs.getString("PROCEDURE_NAME"));
        retValue.setSchemaName(rs.getString("PROCEDURE_SCHEM"));

        return retValue;
    }

    private void mapParams(PreparedStatement stmt,
            MapSqlParameterSource paramSource,
            Map<String, SqlCallParameter> paramOrder)
            throws SQLException {

        Map<String, Object> values = paramSource.getValues();
        for (Map.Entry<String, SqlCallParameter> paramOrderEntry : paramOrder.entrySet()) {
            String paramName = paramOrderEntry.getKey();
            Object value = values.get(paramName);
            if (value == null && paramName.startsWith(dbEngineDialect.getParamNamePrefix())) {
                value = values.get(paramName.substring(dbEngineDialect.getParamNamePrefix().length()));
            }

            SqlCallParameter sqlParam = paramOrderEntry.getValue();

            if (value != null) {
                if (value.getClass().isEnum()) {
                    try {
                        Method method = value.getClass().getMethod("getValue");
                        value = method.invoke(value);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                        log.error("Error mapping enum type '{}': {}", value, ex.getMessage());
                        log.debug("Exception", ex);
                    }
                }

                if (value instanceof Guid) {
                    value = value.toString();
                }

                if (sqlParam.getDataType() == Types.TIMESTAMP) {
                    value = new Timestamp(((Date) value).getTime());
                }

                if (value instanceof Map) {
                    value = SerializationFactory.getSerializer().serialize(value);
                }
            } else {
                if (sqlParam.getDataType() == Types.BOOLEAN || sqlParam.getDataType() == Types.BIT) {
                    value = false;
                }
            }

            int ordinal = sqlParam.getOrdinal();
            try {
                stmt.setObject(ordinal, value);
            } catch (Exception e) {
                log.error("Can't map '{}' of type '{}' to type '{}', mapping to null value for parameter '{}'.",
                        value,
                        value != null ? value.getClass().getName() : null,
                        sqlParam.getDataType(),
                        sqlParam.getName());
                stmt.setObject(ordinal, null);
            }
        }

        log.debug("Mapped params: {}", values.keySet());
    }

    private  static class ProcData {
        private String colName;
        private int ordinal;
        private int dataType;
        private String procName;
        private String schemaName;
        private String dbTypeName;

        public String getColName() {
            return colName;
        }

        public void setColName(String colName) {
            this.colName = colName;
        }

        public int getOrdinal() {
            return ordinal;
        }

        public void setOrdinal(int ordinal) {
            this.ordinal = ordinal;
        }

        public int getDataType() {
            return dataType;
        }

        public void setDataType(int dataType) {
            this.dataType = dataType;
        }

        public String getProcName() {
            return procName;
        }

        public void setProcName(String procName) {
            this.procName = procName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getDbTypeName() {
            return dbTypeName;
        }

        public void setDbTypeName(String dbTypeName) {
            this.dbTypeName = dbTypeName;
        }

    }
}
