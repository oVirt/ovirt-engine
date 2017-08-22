package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * {@code AuditLogDaoImpl} provides a concrete implementation of {@link AuditLogDao}.
 */
@Named
@Singleton
public class AuditLogDaoImpl extends BaseDao implements AuditLogDao {

    private static final RowMapper<AuditLog> auditLogRowMapper = (rs, rowNum) -> {
        AuditLog entity = new AuditLog();
        entity.setAuditLogId(rs.getLong("audit_log_id"));
        entity.setLogTime(DbFacadeUtils.fromDate(rs.getTimestamp("log_time")));
        entity.setLogType(AuditLogType.forValue(rs.getInt("log_type")));
        entity.setSeverity(AuditLogSeverity.forValue(rs.getInt("severity")));
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
        entity.setStorageDomainName(rs.getString("storage_domain_name"));
        entity.setClusterId(getGuid(rs, "cluster_id"));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setCorrelationId(rs.getString("correlation_id"));
        entity.setJobId(getGuid(rs, "job_id"));
        entity.setQuotaId(getGuid(rs, "quota_id"));
        entity.setQuotaName(rs.getString("quota_name"));
        entity.setGlusterVolumeId(getGuid(rs, "gluster_volume_id"));
        entity.setGlusterVolumeName(rs.getString("gluster_volume_name"));
        entity.setOrigin(rs.getString("origin"));
        entity.setCustomId(rs.getString("custom_id"));
        entity.setCustomEventId(rs.getInt("custom_event_id"));
        entity.setEventFloodInSec(rs.getInt("event_flood_in_sec"));
        entity.setCustomData(rs.getString("custom_data"));
        entity.setDeleted(rs.getBoolean("deleted"));
        entity.setCallStack(rs.getString("call_stack"));
        entity.setBrickId(getGuid(rs, "brick_id"));
        entity.setBrickPath(rs.getString("brick_path"));
        return entity;
    };

    private final DbEngineDialect dbEngineDialect;

    @Inject
    AuditLogDaoImpl(DbEngineDialect dbEngineDialect) {
        Objects.requireNonNull(dbEngineDialect, "dbEngineDialect cannot be null");

        this.dbEngineDialect = dbEngineDialect;
    }

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
        return getJdbcTemplate().query(query, auditLogRowMapper);
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
    public List<AuditLog> getByVolumeIdAndType(Guid volumeId, int type) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("gluster_volume_id", volumeId)
                        .addValue("log_type", type);
        return getCallsHandler().executeReadList("GetAuditLogByVolumeIdAndType", auditLogRowMapper, parameterSource);
    }

    @Override
    public void removeAllofTypeForBrick(Guid brickId, int logType){
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("brick_id", brickId)
                        .addValue("audit_log_type", logType);
        getCallsHandler().executeModification("RemoveAuditLogByBrickIdLogType", parameterSource);
    }

    @Override
    public void save(AuditLog event) {
        Map<String, Object> outParameters =
                getCallsHandler().executeModification("InsertAuditLog", getSqlMapper(event));
        event.setAuditLogId((Long) outParameters.get("audit_log_id"));
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
                .addValue("cluster_id", event.getClusterId())
                .addValue("cluster_name", event.getClusterName())
                .addValue("correlation_id", event.getCorrelationId())
                .addValue("job_id", event.getJobId())
                .addValue("quota_id", event.getQuotaId())
                .addValue("quota_name", event.getQuotaName())
                .addValue("gluster_volume_id", event.getGlusterVolumeId())
                .addValue("gluster_volume_name", event.getGlusterVolumeName())
                .addValue("call_stack", event.getCallStack())
                .addValue("repeatable", event.isRepeatable())
                .addValue("brick_id", event.getBrickId())
                .addValue("brick_path", event.getBrickPath())
                .addValue("origin", event.getOrigin())
                .addValue("custom_id", event.getCustomId())
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
    public void removeAllOfTypeForVolume(Guid volumeId, int type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("gluster_volume_id", volumeId).addValue("log_type", type);

        getCallsHandler().executeModification("DeleteAuditAlertLogByVolumeIDAndType", parameterSource);
    }

    @Override
    public int getTimeToWaitForNextPmOp(String vdsName, String event) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_name", vdsName)
                .addValue("event", event)
                .addValue("wait_for_sec", Config.getValue(ConfigValues.FenceQuietTimeBetweenOperationsInSec));

        Map<String, Object> dbResults =
                new SimpleJdbcCall(getJdbcTemplate()).withFunctionName("get_seconds_to_wait_before_pm_operation")
                        .execute(
                        parameterSource);

        String resultKey = dbEngineDialect.getFunctionReturnKey();
        return (Integer) dbResults.getOrDefault(resultKey, 0);
    }

    @Override
    public void clearAllEvents() {
        getCallsHandler().executeModification("ClearAllAuditLogEvents",
                getCustomMapSqlParameterSource().addValue("severity", AuditLogSeverity.ALERT));
    }

    @Override
    public void displayAllEvents() {
        getCallsHandler().executeModification("DisplayAllAuditLogEvents",
                getCustomMapSqlParameterSource().addValue("severity", AuditLogSeverity.ALERT));
    }

    @Override
    public void clearAllAlerts() {
        getCallsHandler().executeModification("SetAllAuditLogAlerts",
                getCustomMapSqlParameterSource().addValue("severity", AuditLogSeverity.ALERT)
                        .addValue("value", true));
    }

    @Override
    public void displayAllAlerts() {
        getCallsHandler().executeModification("SetAllAuditLogAlerts",
                getCustomMapSqlParameterSource().addValue("severity", AuditLogSeverity.ALERT)
                        .addValue("value", false));
    }

    @Override
    public void deleteBackupRelatedAlerts() {
        getCallsHandler().executeModification("DeleteBackupRelatedAlerts", getCustomMapSqlParameterSource());
    }
}
