package org.ovirt.engine.core.dao;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class EngineBackupLogDaoImpl extends BaseDao implements EngineBackupLogDao {

    private static final RowMapper<EngineBackupLog> engineBackupLogRowMapper = (rs, numRow) -> {
        EngineBackupLog entity = new EngineBackupLog();
        entity.setScope(rs.getString("scope"));
        entity.setDoneAt(DbFacadeUtils.fromDate(rs.getTimestamp("done_at")));
        entity.setPassed(rs.getBoolean("is_passed"));
        entity.setFqdn(rs.getString("fqdn"));
        entity.setOutputMessage(rs.getString("output_message"));
        entity.setLogPath(rs.getString("log_path"));
        return entity;
    };

    @Override
    public EngineBackupLog getLastSuccessfulEngineBackup(String scope) {
        return getCallsHandler().executeRead("GetLastSuccessfulEngineBackup", engineBackupLogRowMapper,
                getCustomMapSqlParameterSource().addValue("scope", scope));
    }


    @Override
    public void save(EngineBackupLog engineBackupLog) {

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("scope", engineBackupLog.getScope())
                .addValue("done_at", engineBackupLog.getDoneAt())
                .addValue("status", engineBackupLog.isPassed() ? 1 : -1)
                .addValue("fqdn", engineBackupLog.getFqdn())
                .addValue("output_message", engineBackupLog.getOutputMessage())
                .addValue("log_path", engineBackupLog.getLogPath());

        getCallsHandler().executeModification("LogEngineBackupEvent", parameterSource);
    }
}
