package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>AuditLogDAODbFacadeImpl</code> provides a concrete implementation of {@link AuditLogDAO}. It uses code
 * refactored from {@link DbFacade}.
 */
public class AuditLogDAODbFacadeImpl extends BaseDAODbFacade implements AuditLogDAO {

    @SuppressWarnings("synthetic-access")
    private static final AuditLogRowMapper auditLogRowMapper = new AuditLogRowMapper();

    @Override
    public AuditLog get(long id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("audit_log_id", id);

        return getCallsHandler().executeRead("GetAuditLogByAuditLogId", auditLogRowMapper, parameterSource);
    }

    @Override
    public AuditLog getByOriginAndCustomEventId(String origin, int customEventId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("origin", origin)
                .addValue("custom_event_id", customEventId);

        return getCallsHandler().executeRead("GetAuditLogByOriginAndCustomEventId", auditLogRowMapper, parameterSource);
    }

    @Override
    public List<AuditLog> getAllAfterDate(Date cutoff) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("date", cutoff);

        return getCallsHandler().executeReadList("GetAuditLogLaterThenDate", auditLogRowMapper, parameterSource);
    }

    @Override
    public List<AuditLog> getAllWithQuery(String query) {
        return jdbcTemplate.query(query, auditLogRowMapper);
    }

    @Override
    public List<AuditLog> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromAuditLog", auditLogRowMapper, parameterSource);
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId) {
        return getAllByVMId(vmId, null, false);
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAuditLogByVMId", auditLogRowMapper, parameterSource);
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmId) {
        return getAllByVMTemplateId(vmId, null, false);
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("vm_template_id", vmId)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAuditLogByVMTemplateId", auditLogRowMapper, parameterSource);
    }

    @Override
    public void save(AuditLog event) {
        if (event.isExternal()) {
            getCallsHandler().executeModification("InsertExternalAuditLog", getExternalEventSqlMapper(event));
        }
        else {
            getCallsHandler().executeModification("InsertAuditLog", getSqlMapper(event));
        }
    }

    @Override
    public void update(AuditLog event) {
        throw new UnsupportedOperationException();
    }

    private MapSqlParameterSource getSqlMapper(AuditLog event) {
        return getCustomMapSqlParameterSource()
                .addValue("audit_log_id", event.getAuditLogId())
                .addValue("log_time", event.getLogTime())
                .addValue("log_type", event.getLogType())
                .addValue("log_type_name", event.getLogTypeName())
                .addValue("severity", event.getSeverity())
                .addValue("message", event.getMessage())
                .addValue("user_id", event.getUserId())
                .addValue("user_name", event.getUserName())
                .addValue("vds_id", event.getVdsId())
                .addValue("vds_name", event.getVdsName())
                .addValue("vm_id", event.getVmId())
                .addValue("vm_name", event.getVmName())
                .addValue("vm_template_id", event.getVmTemplateId())
                .addValue("vm_template_name", event.getVmTemplateName())
                .addValue("storage_pool_id", event.getStoragePoolId())
                .addValue("storage_pool_name", event.getStoragePoolName())
                .addValue("storage_domain_id", event.getStorageDomainId())
                .addValue("storage_domain_name", event.getStorageDomainName())
                .addValue("vds_group_id", event.getVdsGroupId())
                .addValue("vds_group_name", event.getVdsGroupName())
                .addValue("correlation_id", event.getCorrelationId())
                .addValue("job_id", event.getJobId())
                .addValue("quota_id", event.getQuotaId())
                .addValue("quota_name", event.getQuotaName())
                .addValue("gluster_volume_id", event.getGlusterVolumeId())
                .addValue("gluster_volume_name", event.getGlusterVolumeName())
                .addValue("call_stack", event.getCallStack())
                .addValue("repeatable", event.isRepeatable());
    }

    private MapSqlParameterSource getExternalEventSqlMapper(AuditLog event) {
        return getSqlMapper(event)
                .addValue("origin", event.getOrigin())
                .addValue("custom_event_id", event.getCustomEventId())
                .addValue("event_flood_in_sec", event.getEventFloodInSec())
                .addValue("custom_data", event.getCustomData());
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
                .addValue("wait_for_sec", Config.getValue(ConfigValues.FenceQuietTimeBetweenOperationsInSec));

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withFunctionName("get_seconds_to_wait_before_pm_operation").execute(
                        parameterSource);

        String resultKey = DbFacade.getInstance().getDbEngineDialect().getFunctionReturnKey();
        return dbResults.get(resultKey) != null ? ((Integer) dbResults.get(resultKey)).intValue() : 0;
    }

    @Override
    public void clearAllDismissed() {
        getCallsHandler().executeModification("ClearAllDismissedAuditLogs", getCustomMapSqlParameterSource());
    }

    private static class AuditLogRowMapper implements RowMapper<AuditLog> {

        @Override
        public AuditLog mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            AuditLog entity = new AuditLog();
            entity.setAuditLogId(rs.getLong("audit_log_id"));
            entity.setLogTime(DbFacadeUtils.fromDate(rs
                    .getTimestamp("log_time")));
            entity.setLogType(AuditLogType.forValue(rs.getInt("log_type")));
            entity.setSeverity(AuditLogSeverity.forValue(rs
                    .getInt("severity")));
            entity.setMessage(rs.getString("message"));
            entity.setUserId(getGuid(rs, "user_id"));
            entity.setUserName(rs.getString("user_name"));
            entity.setVdsId(getGuid(rs, "vds_id"));
            entity.setVdsName(rs.getString("vds_name"));
            entity.setVmId(getGuid(rs, "vm_id"));
            entity.setVmName(rs.getString("vm_name"));
            entity.setVmTemplateId(getGuid(rs, "vm_template_id"));
            entity.setVmTemplateName(rs.getString("vm_template_name"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs.getString("storage_pool_name"));
            entity.setStorageDomainId(getGuid(rs, "storage_domain_id"));
            entity.setStorageDomainName(rs
                    .getString("storage_domain_name"));
            entity.setVdsGroupId(getGuid(rs, "vds_group_id"));
            entity.setVdsGroupName(rs
                    .getString("vds_group_name"));
            entity.setCorrelationId(rs.getString("correlation_id"));
            entity.setJobId(getGuid(rs, "job_id"));
            entity.setQuotaId(getGuid(rs, "quota_id"));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setGlusterVolumeId(getGuid(rs, "gluster_volume_id"));
            entity.setGlusterVolumeName(rs.getString("gluster_volume_name"));
            entity.setOrigin(rs.getString("origin"));
            entity.setCustomEventId(rs.getInt("custom_event_id"));
            entity.setEventFloodInSec(rs.getInt("event_flood_in_sec"));
            entity.setCustomData(rs.getString("custom_data"));
            entity.setDeleted(rs.getBoolean("deleted"));
            entity.setCallStack(rs.getString("call_stack"));
            return entity;
        }
    }
}
