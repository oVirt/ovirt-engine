package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GeoRepCrawlStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation of the DB Facade for Gluster Geo Replication.
 */
@Named
@Singleton
public class GlusterGeoRepDaoImpl extends MassOperationsGenericDao<GlusterGeoRepSession, Guid> implements
        GlusterGeoRepDao {

    private static final RowMapper<GlusterGeoRepSession> georepSessionRowMapper = new GeoRepSessionRowMapper();
    private static final RowMapper<GlusterGeoRepSessionConfiguration> georepSessionConfigRowMapper = new GeoRepSessionConfigRowMapper();
    private static final RowMapper<GlusterGeoRepSessionDetails> georepSessionDetailsRowMapper = new GeoRepSessionDetailsRowMapper();
    private static final RowMapper<GlusterGeoRepSessionConfiguration> geoRepSessionConfigMasterRowMapper = new GeoRepSessionConfigMasterRowMapper();

    public GlusterGeoRepDaoImpl() {
        super("GlusterGeoRepSession");
        setProcedureNameForGet("GetGlusterGeoRepSessionById");
    }

    private static final class GeoRepSessionRowMapper implements RowMapper<GlusterGeoRepSession> {
        @Override
        public GlusterGeoRepSession mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterGeoRepSession entity = new GlusterGeoRepSession();
            entity.setId(getGuidDefaultEmpty(rs, "session_id"));
            entity.setMasterVolumeId(getGuidDefaultEmpty(rs, "master_volume_id"));
            entity.setMasterVolumeName(rs.getString("master_volume_name"));
            entity.setSessionKey(rs.getString("session_key"));
            entity.setSlaveHostName(rs.getString("slave_host_name"));
            entity.setSlaveNodeUuid(getGuid(rs, "slave_host_uuid"));
            entity.setSlaveVolumeId(getGuid(rs, "slave_volume_id"));
            entity.setSlaveVolumeName(rs.getString("slave_volume_name"));
            entity.setUserName(rs.getString("user_name"));
            entity.setStatus(GeoRepSessionStatus.valueOf(rs.getString("status")));
            return entity;
        }
    }

    private static final class GeoRepSessionConfigRowMapper extends GeoRepSessionConfigMasterRowMapper implements RowMapper<GlusterGeoRepSessionConfiguration> {
        @Override
        public GlusterGeoRepSessionConfiguration mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterGeoRepSessionConfiguration entity = super.mapRow(rs, rowNum);
            entity.setId(getGuidDefaultEmpty(rs, "session_id"));
            entity.setValue(rs.getString("config_value"));
            return entity;
        }
    }

    private static class GeoRepSessionConfigMasterRowMapper implements RowMapper<GlusterGeoRepSessionConfiguration> {
        private Guid sessionId;

        public void setSessionId(Guid sessionId) {
            this.sessionId = sessionId;
        }

        @Override
        public GlusterGeoRepSessionConfiguration mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterGeoRepSessionConfiguration entity = new GlusterGeoRepSessionConfiguration();
            entity.setId(sessionId);
            entity.setKey(rs.getString("config_key"));
            entity.setDescription(rs.getString("config_description"));
            entity.setAllowedValues(rs.getString("config_possible_values") != null ? Arrays.asList(rs.getString("config_possible_values")
                    .split(";"))
                    : null);
            return entity;
        }
    }

    private static final class GeoRepSessionDetailsRowMapper implements RowMapper<GlusterGeoRepSessionDetails> {
        @Override
        public GlusterGeoRepSessionDetails mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterGeoRepSessionDetails entity = new GlusterGeoRepSessionDetails();
            entity.setSessionId(getGuidDefaultEmpty(rs, "session_id"));
            entity.setMasterBrickId(getGuidDefaultEmpty(rs, "master_brick_id"));
            entity.setSlaveHostName(rs.getString("slave_host_name"));
            entity.setSlaveNodeUuid(getGuid(rs, "slave_host_uuid"));
            entity.setStatus(GeoRepSessionStatus.valueOf(rs.getString("status")));
            entity.setCheckPointStatus(rs.getString("checkpoint_status"));
            entity.setCrawlStatus(GeoRepCrawlStatus.valueOf(rs.getString("crawl_status")));
            entity.setDataOpsPending(rs.getLong("data_pending"));
            entity.setMetaOpsPending(rs.getLong("meta_pending"));
            entity.setEntryOpsPending(rs.getLong("entry_pending"));
            entity.setFailures(rs.getLong("failures"));
            entity.setCheckpointCompleted(rs.getBoolean("is_checkpoint_completed"));
            entity.setCheckPointCompletedAt(DbFacadeUtils.fromDate(rs.getTimestamp("checkpoint_completed_time")));
            entity.setCheckPointTime(DbFacadeUtils.fromDate(rs.getTimestamp("checkpoint_time")));
            entity.setLastSyncedAt(DbFacadeUtils.fromDate(rs.getTimestamp("last_synced_at")));
            return entity;
        }
    }

    @Override
    protected RowMapper<GlusterGeoRepSession> createEntityRowMapper() {
        return georepSessionRowMapper;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("session_id", id);
    }

    @Override
    public void save(GlusterGeoRepSession geoRepSession) {
        getCallsHandler().executeModification("InsertGlusterGeoRepSession", createFullParametersMapper(geoRepSession));
    }

    @Override
    public void saveDetails(GlusterGeoRepSessionDetails geoRepSessionDetails) {
        getCallsHandler().executeModification("InsertGlusterGeoRepSessionDetail", createFullParametersMapper(geoRepSessionDetails));
    }

    @Override
    public void saveConfig(GlusterGeoRepSessionConfiguration geoRepSessionConfig) {
        getCallsHandler().executeModification("InsertGlusterGeoRepSessionConfig", createFullParametersMapper(geoRepSessionConfig));
    }

    @Override
    public GlusterGeoRepSession getGeoRepSession(String sessionKey) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionByKey", georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("session_key", sessionKey));
    }

    @Override
    public GlusterGeoRepSession getGeoRepSession(Guid masterVolumeId, Guid slaveHostId, String slaveVolumeName) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionBySlaveHostAndVolume", georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("master_volume_id", masterVolumeId)
                .addValue("slave_host_uuid", slaveHostId)
                .addValue("slave_volume_name", slaveVolumeName));
    }

    @Override
    public GlusterGeoRepSession getGeoRepSession(Guid masterVolumeId, String slaveHostName, String slaveVolumeName) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionBySlaveHostNameAndVolume", georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("master_volume_id", masterVolumeId)
                .addValue("slave_host_name", slaveHostName)
                .addValue("slave_volume_name", slaveVolumeName));
    }


    @Override
    public List<GlusterGeoRepSession> getGeoRepSessions(Guid masterVolumeId) {
        return getCallsHandler().executeReadList("GetGlusterGeoRepSessionsByVolumeId", georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("master_volume_id", masterVolumeId));
    }

    @Override
    public List<GlusterGeoRepSession> getGeoRepSessionsInCluster(Guid clusterId) {
        return getCallsHandler().executeReadList("GetGlusterGeoRepSessionsByClusterId", georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public GlusterGeoRepSession getGeoRepSessionBySlaveVolume(Guid slaveVolumeId) {
        return getCallsHandler().executeRead("GetGeoRepSessionBySlaveVolume",
                georepSessionRowMapper,
                getCustomMapSqlParameterSource().addValue("slave_volume_id", slaveVolumeId));
    }

    @Override
    public List<GlusterGeoRepSessionDetails> getGeoRepSessionDetails(Guid sessionId) {
        return getCallsHandler().executeReadList("GetGlusterGeoRepSessionDetails", georepSessionDetailsRowMapper,
                createIdParameterMapper(sessionId));
    }

    @Override
    public GlusterGeoRepSessionDetails getGeoRepSessionDetails(Guid sessionId, Guid masterBrickId) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionDetailsForBrick", georepSessionDetailsRowMapper,
                createIdParameterMapper(sessionId).addValue("master_brick_id", masterBrickId));
    }

    @Override
    public List<GlusterGeoRepSessionConfiguration> getGeoRepSessionConfig(Guid sessionId) {
        return getCallsHandler().executeReadList("GetGlusterGeoRepSessionConfig", georepSessionConfigRowMapper,
                createIdParameterMapper(sessionId));
    }

    @Override
    public List<GlusterGeoRepSessionConfiguration> getGlusterGeoRepSessionUnSetConfig(Guid sessionId) {
        ((GeoRepSessionConfigMasterRowMapper)geoRepSessionConfigMasterRowMapper).setSessionId(sessionId);
        return getCallsHandler().executeReadList("GetGlusterGeoRepSessionUnSetConfig", geoRepSessionConfigMasterRowMapper, createIdParameterMapper(sessionId));
    }

    @Override
    public GlusterGeoRepSessionConfiguration getGeoRepSessionConfigByKey(Guid sessionId, String configKey) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionConfigByKey", georepSessionConfigRowMapper,
                createIdParameterMapper(sessionId).addValue("config_key", configKey));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(
            GlusterGeoRepSession geoRepSession) {
        return createIdParameterMapper(geoRepSession.getId())
                .addValue("master_volume_id", geoRepSession.getMasterVolumeId())
                .addValue("session_key", geoRepSession.getSessionKey())
                .addValue("slave_host_name", geoRepSession.getSlaveHostName())
                .addValue("slave_host_uuid", geoRepSession.getSlaveNodeUuid())
                .addValue("slave_volume_name", geoRepSession.getSlaveVolumeName())
                .addValue("slave_volume_id", geoRepSession.getSlaveVolumeId())
                .addValue("user_name", geoRepSession.getUserName())
                .addValue("status", EnumUtils.nameOrNull(geoRepSession.getStatus()));
    }


    protected MapSqlParameterSource createFullParametersMapper(
            GlusterGeoRepSessionDetails geoRepSessionDetails) {
        return createIdParameterMapper(geoRepSessionDetails.getSessionId())
                .addValue("master_brick_id", geoRepSessionDetails.getMasterBrickId())
                .addValue("slave_host_name", geoRepSessionDetails.getSlaveHostName())
                .addValue("slave_host_uuid", geoRepSessionDetails.getSlaveNodeUuid())
                .addValue("status", EnumUtils.nameOrNull(geoRepSessionDetails.getStatus()))
                .addValue("checkpoint_status", geoRepSessionDetails.getCheckPointStatus())
                .addValue("crawl_status", EnumUtils.nameOrNull(geoRepSessionDetails.getCrawlStatus()))
                .addValue("data_pending", geoRepSessionDetails.getDataOpsPending())
                .addValue("meta_pending", geoRepSessionDetails.getMetaOpsPending())
                .addValue("entry_pending", geoRepSessionDetails.getEntryOpsPending())
                .addValue("failures", geoRepSessionDetails.getFailures())
                .addValue("is_checkpoint_completed", geoRepSessionDetails.isCheckpointCompleted())
                .addValue("checkpoint_completed_time", geoRepSessionDetails.getCheckPointCompletedAt())
                .addValue("checkpoint_time", geoRepSessionDetails.getCheckPointTime())
                .addValue("last_synced_at", geoRepSessionDetails.getLastSyncedAt());
    }


    protected MapSqlParameterSource createFullParametersMapper(
            GlusterGeoRepSessionConfiguration geoRepSessionConfig) {
        return createIdParameterMapper(geoRepSessionConfig.getId())
                .addValue("config_key", geoRepSessionConfig.getKey())
                .addValue("config_value", geoRepSessionConfig.getValue())
                .addValue("config_description", geoRepSessionConfig.getDescription())
                .addValue("config_possible_values", geoRepSessionConfig.getAllowedValues());
    }

    @Override
    public GlusterGeoRepSession getById(Guid id) {
        return getCallsHandler().executeRead("GetGlusterGeoRepSessionById", georepSessionRowMapper,
                createIdParameterMapper(id));
    }

    @Override
    public void updateSession(GlusterGeoRepSession geoRepSession) {
        getCallsHandler().executeModification("UpdateGlusterGeoRepSession",
                createIdParameterMapper(geoRepSession.getId())
                        .addValue("status", EnumUtils.nameOrNull(geoRepSession.getStatus()))
                        .addValue("slave_host_uuid", geoRepSession.getSlaveNodeUuid())
                        .addValue("slave_volume_id", geoRepSession.getSlaveVolumeId()));
    }

    @Override
    public void remove(Guid sessonId) {
        getCallsHandler().executeModification("DeleteGlusterGeoRepSession", getCustomMapSqlParameterSource().addValue("session_id", sessonId));
    }

    @Override
    public void updateDetails(GlusterGeoRepSessionDetails geoRepSessionDetails) {
        getCallsHandler().executeModification("UpdateGlusterGeoRepSessionDetail",
                createFullParametersMapper(geoRepSessionDetails));

    }

    @Override
    public void updateConfig(GlusterGeoRepSessionConfiguration geoRepSessionConfig) {
        getCallsHandler().executeModification("UpdateGlusterGeoRepSessionConfig",
                createFullParametersMapper(geoRepSessionConfig));

    }

    @Override
    public void saveOrUpdateDetailsInBatch(List<GlusterGeoRepSessionDetails> geoRepSessionDetailsObjs) {
        List<GlusterGeoRepSessionDetails> insertList = new ArrayList<>();
        List<GlusterGeoRepSessionDetails> updateList = new ArrayList<>();
        for (GlusterGeoRepSessionDetails details : geoRepSessionDetailsObjs) {
            if (getGeoRepSessionDetails(details.getSessionId(), details.getMasterBrickId()) == null) {
                insertList.add(details);
            } else {
                updateList.add(details);
            }
        }
        saveDetailsInBatch(insertList);
        updateDetailsInBatch(updateList);
    }

    @Override
    public void saveDetailsInBatch(List<GlusterGeoRepSessionDetails> geoRepSessionDetailsList) {
        getCallsHandler().executeStoredProcAsBatch("InsertGlusterGeoRepSessionDetail",
                geoRepSessionDetailsList, getDetailsBatchMapper());
    }

    @Override
    public void updateDetailsInBatch(List<GlusterGeoRepSessionDetails> geoRepSessionDetailsObjs) {
        getCallsHandler().executeStoredProcAsBatch("UpdateGlusterGeoRepSessionDetail",
                geoRepSessionDetailsObjs, getDetailsBatchMapper());
    }

    public MapSqlParameterMapper<GlusterGeoRepSessionDetails> getDetailsBatchMapper() {
        return this::createFullParametersMapper;
    }

    @Override
    public List<GlusterGeoRepSession> getAllSessions() {
        return getCallsHandler().executeReadList("GetAllGlusterGeoRepSessions", georepSessionRowMapper, getCustomMapSqlParameterSource());
    }
}
