package org.ovirt.engine.core.dal.dbbroker;

import java.util.Map;

public class StoredProcedureMetaData {
    private String schemaName;
    private String dbName;
    private String sqlCommand;
    private Map<String, SqlCallParameter> paramatersMetaData;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSqlCommand() {
        return sqlCommand;
    }

    public void setSqlCommand(String sqlCommand) {
        this.sqlCommand = sqlCommand;
    }

    public Map<String, SqlCallParameter> getParamatersMetaData() {
        return paramatersMetaData;
    }

    public void setParamatersMetaData(
            Map<String, SqlCallParameter> paramatersMetaData) {
        this.paramatersMetaData = paramatersMetaData;
    }

}
