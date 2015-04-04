package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;

@Named
@Singleton
public class EngineBackupLogDaoDbFacadeImpl extends BaseDAODbFacade implements EngineBackupLogDao {

    private static class EngineBackupLogRowMapper implements RowMapper<EngineBackupLog> {
        public static final EngineBackupLogRowMapper INSTANCE = new EngineBackupLogRowMapper();

        @Override
        public EngineBackupLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            EngineBackupLog entity = new EngineBackupLog();
            entity.setDbName(rs.getString("db_name"));
            entity.setDoneAt(DbFacadeUtils.fromDate(rs.getTimestamp("done_at")));
            entity.setPassed(rs.getBoolean("is_passed"));
            entity.setOutputMessage(rs.getString("output_message"));
            return entity;
        }
    }

    @Override
    public EngineBackupLog getLastSuccessfulEngineBackup(String dbName) {
        return getCallsHandler().executeRead("GetLastSuccessfulEngineBackup", EngineBackupLogRowMapper.INSTANCE,
               getCustomMapSqlParameterSource().addValue("db_name", dbName));
    }


    @Override
    public void save(EngineBackupLog engineBackupLog) {

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("db_name", engineBackupLog.getDbName())
                .addValue("done_at", engineBackupLog.getDoneAt())
                .addValue("status", engineBackupLog.isPassed() ? 1 : -1)
                .addValue("output_message", engineBackupLog.getOutputMessage());

        getCallsHandler().executeModification("LogEngineBackupEvent", parameterSource);
    }
}
