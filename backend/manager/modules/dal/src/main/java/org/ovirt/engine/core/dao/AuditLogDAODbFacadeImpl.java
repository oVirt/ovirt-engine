package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;

/**
 * ,code>AuditLogDAODbFacadeImpl</code> provides a concrete implementation of {@link AuditLogDAO}. It uses code
 * refactored from {@link DbFacade}.
 *
 *
 */
public class AuditLogDAODbFacadeImpl extends BaseDAODbFacade implements AuditLogDAO {

    @Override
    public AuditLog get(long id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("audit_log_id", id);

        ParameterizedRowMapper<AuditLog> mapper = new ParameterizedRowMapper<AuditLog>() {
            @Override
            public AuditLog mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                AuditLog entity = new AuditLog();
                entity.setaudit_log_id(rs.getLong("audit_log_id"));
                entity.setlog_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("log_time")));
                entity.setlog_type(AuditLogType.forValue(rs.getInt("log_type")));
                entity.setseverity(AuditLogSeverity.forValue(rs
                        .getInt("severity")));
                entity.setmessage(rs.getString("message"));
                entity.setuser_id(NGuid.createGuidFromString(rs
                        .getString("user_id")));
                entity.setuser_name(rs.getString("user_name"));
                entity.setvds_id(NGuid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setvm_id(NGuid.createGuidFromString(rs
                        .getString("vm_id")));
                entity.setvm_name(rs.getString("vm_name"));
                entity.setvm_template_id(NGuid.createGuidFromString(rs
                        .getString("vm_template_id")));
                entity.setvm_template_name(rs.getString("vm_template_name"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setstorage_domain_id(NGuid.createGuidFromString(rs
                        .getString("storage_domain_id")));
                entity.setstorage_domain_name(rs
                        .getString("storage_domain_name"));
                entity.setvds_group_id(NGuid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs
                        .getString("vds_group_name"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetAuditLogByAuditLogId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AuditLog> getAllAfterDate(Date cutoff) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("date", cutoff);

        ParameterizedRowMapper<AuditLog> mapper = new ParameterizedRowMapper<AuditLog>() {
            @Override
            public AuditLog mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                AuditLog entity = new AuditLog();
                entity.setaudit_log_id(rs.getLong("audit_log_id"));
                entity.setlog_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("log_time")));
                entity.setlog_type(AuditLogType.forValue(rs.getInt("log_type")));
                entity.setseverity(AuditLogSeverity.forValue(rs
                        .getInt("severity")));
                entity.setmessage(rs.getString("message"));
                entity.setuser_id(NGuid.createGuidFromString(rs
                        .getString("user_id")));
                entity.setuser_name(rs.getString("user_name"));
                entity.setvds_id(NGuid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setvm_id(NGuid.createGuidFromString(rs
                        .getString("vm_id")));
                entity.setvm_name(rs.getString("vm_name"));
                entity.setvm_template_id(NGuid.createGuidFromString(rs
                        .getString("vm_template_id")));
                entity.setvm_template_name(rs.getString("vm_template_name"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setstorage_domain_id(NGuid.createGuidFromString(rs
                        .getString("storage_domain_id")));
                entity.setstorage_domain_name(rs
                        .getString("storage_domain_name"));
                entity.setvds_group_id(NGuid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs
                        .getString("vds_group_name"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAuditLogLaterThenDate", mapper, parameterSource);

    }

    @Override
    public List<AuditLog> getAllWithQuery(String query) {
        ParameterizedRowMapper<AuditLog> mapper = new ParameterizedRowMapper<AuditLog>() {
            @Override
            public AuditLog mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                AuditLog entity = new AuditLog();
                entity.setaudit_log_id(rs.getLong("audit_log_id"));
                entity.setlog_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("log_time")));
                entity.setlog_type(AuditLogType.forValue(rs.getInt("log_type")));
                entity.setseverity(AuditLogSeverity.forValue(rs
                        .getInt("severity")));
                entity.setmessage(rs.getString("message"));
                entity.setuser_id(Guid.createGuidFromString(rs
                        .getString("user_id")));
                entity.setuser_name(rs.getString("user_name"));
                entity.setvds_id(NGuid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                entity.setvm_name(rs.getString("vm_name"));
                entity.setvm_template_id(Guid.createGuidFromString(rs
                        .getString("vm_template_id")));
                entity.setvm_template_name(rs.getString("vm_template_name"));
                entity.setstorage_pool_id(Guid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setstorage_domain_id(Guid.createGuidFromString(rs
                        .getString("storage_domain_id")));
                entity.setstorage_domain_name(rs
                        .getString("storage_domain_name"));
                entity.setvds_group_id(NGuid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs
                        .getString("vds_group_name"));
                return entity;
            }
        };

        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AuditLog> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<AuditLog> mapper = new ParameterizedRowMapper<AuditLog>() {
            @Override
            public AuditLog mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                AuditLog entity = new AuditLog();
                entity.setaudit_log_id(rs.getLong("audit_log_id"));
                entity.setlog_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("log_time")));
                entity.setlog_type(AuditLogType.forValue(rs.getInt("log_type")));
                entity.setseverity(AuditLogSeverity.forValue(rs
                        .getInt("severity")));
                entity.setmessage(rs.getString("message"));
                entity.setuser_id(NGuid.createGuidFromString(rs
                        .getString("user_id")));
                entity.setuser_name(rs.getString("user_name"));
                entity.setvds_id(NGuid.createGuidFromString(rs
                        .getString("vds_id")));
                entity.setvds_name(rs.getString("vds_name"));
                entity.setvm_id(NGuid.createGuidFromString(rs
                        .getString("vm_id")));
                entity.setvm_name(rs.getString("vm_name"));
                entity.setvm_template_id(NGuid.createGuidFromString(rs
                        .getString("vm_template_id")));
                entity.setvm_template_name(rs.getString("vm_template_name"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setstorage_domain_id(NGuid.createGuidFromString(rs
                        .getString("storage_domain_id")));
                entity.setstorage_domain_name(rs
                        .getString("storage_domain_name"));
                entity.setvds_group_id(NGuid.createGuidFromString(rs
                        .getString("vds_group_id")));
                entity.setvds_group_name(rs
                        .getString("vds_group_name"));
                return entity;
            }
        };
        return getCallsHandler().executeReadList("GetAllFromAuditLog", mapper, parameterSource);
    }

    @Override
    public void save(AuditLog event) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("audit_log_id", event.getaudit_log_id())
                .addValue("log_time", event.getlog_time())
                .addValue("log_type", event.getlog_type())
                .addValue("log_type_name", event.getlog_type_name())
                .addValue("severity", event.getseverity())
                .addValue("message", event.getmessage())
                .addValue("user_id", event.getuser_id())
                .addValue("user_name", event.getuser_name())
                .addValue("vds_id", event.getvds_id())
                .addValue("vds_name", event.getvds_name())
                .addValue("vm_id", event.getvm_id())
                .addValue("vm_name", event.getvm_name())
                .addValue("vm_template_id", event.getvm_template_id())
                .addValue("vm_template_name", event.getvm_template_name())
                .addValue("storage_pool_id", event.getstorage_pool_id())
                .addValue("storage_pool_name", event.getstorage_pool_name())
                .addValue("storage_domain_id", event.getstorage_domain_id())
                .addValue("storage_domain_name", event.getstorage_domain_name())
                .addValue("vds_group_id", event.getvds_group_id())
                .addValue("vds_group_name", event.getvds_group_name());

        getCallsHandler().executeModification("InsertAuditLog", parameterSource);
    }

    @Override
    public void update(AuditLog event) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("audit_log_id", event.getaudit_log_id())
                .addValue("log_time", event.getlog_time())
                .addValue("log_type", event.getlog_type())
                .addValue("severity", event.getseverity())
                .addValue("message", event.getmessage())
                .addValue("user_id", event.getuser_id())
                .addValue("user_name", event.getuser_name())
                .addValue("vds_id", event.getvds_id())
                .addValue("vds_name", event.getvds_name())
                .addValue("vm_id", event.getvm_id())
                .addValue("vm_name", event.getvm_name())
                .addValue("vm_template_id", event.getvm_template_id())
                .addValue("vm_template_name", event.getvm_template_name())
                .addValue("storage_pool_id", event.getstorage_pool_id())
                .addValue("storage_pool_name", event.getstorage_pool_name())
                .addValue("storage_domain_id", event.getstorage_domain_id())
                .addValue("storage_domain_name", event.getstorage_domain_name())
                .addValue("vds_group_id", event.getvds_group_id())
                .addValue("vds_group_name", event.getvds_group_name());

        getCallsHandler().executeModification("UpdateAuditLog", parameterSource);
    }

    @Override
    public void remove(long id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("audit_log_id", id);

        getCallsHandler().executeModification("DeleteAuditLog", parameterSource);
    }

    @Override
    public void removeAllBeforeDate(Date cutoff) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("date", cutoff);

        getCallsHandler().executeModification("DeleteAuditLogOlderThenDate", parameterSource);
    }

    @Override
    public void removeAllForVds(Guid id, boolean removeConfigAlerts) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("delete_config_alerts",
                        removeConfigAlerts);

        getCallsHandler().executeModification("DeleteAuditLogAlertsByVdsID", parameterSource);
    }

    @Override
    public void removeAllOfTypeForVds(Guid id, int type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("log_type", type);

        getCallsHandler().executeModification("DeleteAuditAlertLogByVdsIDAndType", parameterSource);
    }

    @Override
    public int getTimeToWaitForNextPmOp(String vdsName, String event) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_name", vdsName)
                .addValue("event", event)
                .addValue("wait_for_sec", Config.GetValue(ConfigValues.FenceQuietTimeBetweenOperationsInSec));

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withFunctionName("get_seconds_to_wait_before_pm_operation").execute(
                        parameterSource);

        String resultKey = DbFacade.getInstance().getDbEngineDialect().getFunctionReturnKey();
        return dbResults.get(resultKey) != null ? ((Integer) dbResults.get(resultKey)).intValue() : 0;
    }
}
