package org.ovirt.engine.core.dao.gluster;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Implementation of the DB Facade for Gluster Volumes.
 */
@Named
@Singleton
public class GlusterVolumeDaoImpl extends MassOperationsGenericDao<GlusterVolumeEntity, Guid> implements
        GlusterVolumeDao {

    private static final RowMapper<GlusterVolumeEntity> volumeRowMapper = (rs, rowNum) -> {
        GlusterVolumeEntity entity = new GlusterVolumeEntity();
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setClusterName(rs.getString("cluster_name"));
        entity.setName(rs.getString("vol_name"));
        entity.setVolumeType(GlusterVolumeType.valueOf(rs.getString("vol_type")));
        entity.setStatus(GlusterStatus.valueOf(rs.getString("status")));
        entity.setReplicaCount(rs.getInt("replica_count"));
        entity.setStripeCount(rs.getInt("stripe_count"));
        entity.setDisperseCount(rs.getInt("disperse_count"));
        entity.setRedundancyCount(rs.getInt("redundancy_count"));
        entity.setSnapshotsCount(rs.getInt("snapshot_count"));
        entity.setSnapshotScheduled(rs.getBoolean("snapshot_scheduled"));
        entity.setIsGeoRepMaster(rs.getBoolean("is_master"));
        entity.setGeoRepMasterVolAndClusterName(rs.getString("master_vol_cluster"));
        entity.setIsArbiter(rs.getBoolean("is_arbiter"));
        return entity;
    };

    private static final RowMapper<AccessProtocol> accessProtocolRowMapper =
            (rs, rowNum) -> AccessProtocol.valueOf(rs.getString("access_protocol"));

    private static final RowMapper<TransportType> transportTypeRowMapper =
            (rs, rowNum) -> TransportType.valueOf(rs.getString("transport_type"));

    private static final RowMapper<GlusterAsyncTask> glusterAsyncTaskRowMapper = (rs, rowNum) -> {
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(getGuid(rs, "external_id"));
        String jobStatus = rs.getString("job_status");
        if (asyncTask.getTaskId() != null || JobExecutionStatus.STARTED.name().equalsIgnoreCase(jobStatus)) {
            asyncTask.setJobId(getGuid(rs, "job_job_id"));
            asyncTask.setJobStatus(JobExecutionStatus.valueOf(jobStatus));
        }
        String stepStatus = rs.getString("status");
        String stepType = rs.getString("step_type");
        if (stepType != null && !stepType.isEmpty()) {
            asyncTask.setType(GlusterTaskType.forValue(StepEnum.valueOf(stepType)));
        }
        if (stepStatus != null && !stepStatus.isEmpty()) {
            asyncTask.setStatus(JobExecutionStatus.valueOf(stepStatus));
        }
        return asyncTask;
    };

    private static final RowMapper<GlusterVolumeAdvancedDetails> glusterVolumesAdvancedDetailsRowMapper = (rs, rowNum) -> {
        GlusterVolumeAdvancedDetails advancedDetails = new GlusterVolumeAdvancedDetails();
        GlusterVolumeSizeInfo capacityInfo = new GlusterVolumeSizeInfo();
        capacityInfo.setVolumeId(getGuid(rs, "volume_id"));
        capacityInfo.setTotalSize(rs.getLong("total_space"));
        capacityInfo.setUsedSize(rs.getLong("used_space"));
        capacityInfo.setFreeSize(rs.getLong("free_space"));
        capacityInfo.setConfirmedFreeSize(
                (rs.getObject("confirmed_free_space") != null) ?
                        rs.getLong("confirmed_Free_space") : null);
        capacityInfo.setVdoSavings(rs.getInt("vdo_savings"));
        advancedDetails.setUpdatedAt(rs.getTimestamp("_update_date"));
        advancedDetails.setCapacityInfo(capacityInfo);
        return advancedDetails;
    };

    @Inject
    private GlusterOptionDao glusterOptionDao;
    @Inject
    private GlusterBrickDao glusterBrickDao;
    @Inject
    private GlusterVolumeSnapshotConfigDao glusterVolumeSnapshotConfigDao;

    public GlusterVolumeDaoImpl() {
        super("GlusterVolume");
        setProcedureNameForGet("GetGlusterVolumeById");
    }

    @Override
    public void save(GlusterVolumeEntity volume) {
        insertVolumeEntity(volume);
        insertVolumeBricks(volume);
        insertVolumeOptions(volume);
        insertVolumeAccessProtocols(volume);
        insertVolumeTransportTypes(volume);
    }

    @Override
    public GlusterVolumeEntity getById(Guid id) {
        GlusterVolumeEntity volume = getCallsHandler().executeRead(
                "GetGlusterVolumeById", volumeRowMapper,
                createVolumeIdParams(id));
        fetchRelatedEntities(volume);
        return volume;
    }

    @Override
    public GlusterVolumeEntity getByName(Guid clusterId, String volName) {
        GlusterVolumeEntity volume = getCallsHandler().executeRead(
                "GetGlusterVolumeByName", volumeRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("vol_name", volName));

        fetchRelatedEntities(volume);
        return volume;
    }

    @Override
    public List<GlusterVolumeEntity> getByClusterId(Guid clusterId) {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesByClusterGuid",
                        volumeRowMapper,
                        getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getVolumesByOption(Guid clusterId,
            GlusterStatus status,
            String optionKey,
            String optionValue) {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesByOption",
                        volumeRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("status", EnumUtils.nameOrNull(status))
                                .addValue("option_key", optionKey)
                                .addValue("option_val", optionValue));
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getVolumesByStatusTypesAndOption(Guid clusterId,
            GlusterStatus status,
            List<GlusterVolumeType> volumeTypes,
            String optionKey,
            String optionValue) {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesByStatusTypesAndOption",
                        volumeRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("status", EnumUtils.nameOrNull(status))
                                .addValue("vol_types", StringUtils.join(volumeTypes, ','))
                                .addValue("option_key", optionKey)
                                .addValue("option_val", optionValue));
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getVolumesByStatusAndTypes(Guid clusterId,
            GlusterStatus status,
            List<GlusterVolumeType> volumeTypes) {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesByStatusAndTypes",
                        volumeRowMapper,
                        getCustomMapSqlParameterSource()
                                .addValue("cluster_id", clusterId)
                                .addValue("status", EnumUtils.nameOrNull(status))
                                .addValue("vol_types", StringUtils.join(volumeTypes, ',')));
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getVolumesSupportedAsStorageDomain() {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesSupportedAsStorageDomain",
                        volumeRowMapper, getCustomMapSqlParameterSource());
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getAllWithQuery(String query) {
        List<GlusterVolumeEntity> volumes = getJdbcTemplate().query(query, volumeRowMapper);
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteGlusterVolumeByGuid",
                createVolumeIdParams(id));
    }

    @Override
    public void removeByName(Guid clusterId, String volName) {
        getCallsHandler().executeModification("DeleteGlusterVolumeByName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("vol_name", volName));
    }

    @Override
    public void removeAll(Collection<Guid> ids) {
        getCallsHandler().executeModification("DeleteGlusterVolumesByGuids",
                getCustomMapSqlParameterSource().addValue("volume_ids", StringUtils.join(ids, ',')));
    }

    @Override
    public void removeByClusterId(Guid clusterId) {
        getCallsHandler().executeModification("DeleteGlusterVolumesByClusterId",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public void updateVolumeStatus(Guid volumeId, GlusterStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeStatus",
                createVolumeIdParams(volumeId).addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateVolumeTask(Guid volumeId, Guid taskId) {
        getCallsHandler().executeModification("UpdateGlusterVolumeAsyncTask",
                createVolumeIdParams(volumeId).addValue("task_id", taskId));
    }

    @Override
    public GlusterVolumeEntity getVolumeByGlusterTask(Guid taskId) {
        GlusterVolumeEntity volume = getCallsHandler().executeRead(
                "GetGlusterVolumeByGlusterTaskId", volumeRowMapper,
                getCustomMapSqlParameterSource().addValue("task_id", taskId));
        fetchRelatedEntities(volume);
        return volume;
    }

    @Override
    public void updateVolumeStatusByName(Guid clusterId, String volumeName, GlusterStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeStatusByName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("vol_name", volumeName)
                        .addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void addAccessProtocol(Guid volumeId, AccessProtocol protocol) {
        getCallsHandler().executeModification("InsertGlusterVolumeAccessProtocol",
                createAccessProtocolParams(volumeId, protocol));
    }

    @Override
    public void removeAccessProtocol(Guid volumeId, AccessProtocol protocol) {
        getCallsHandler().executeModification("DeleteGlusterVolumeAccessProtocol",
                createAccessProtocolParams(volumeId, protocol));
    }

    @Override
    public void addTransportType(Guid volumeId, TransportType transportType) {
        getCallsHandler().executeModification("InsertGlusterVolumeTransportType",
                createTransportTypeParams(volumeId, transportType));
    }

    @Override
    public void removeTransportType(Guid volumeId, TransportType transportType) {
        getCallsHandler().executeModification("DeleteGlusterVolumeTransportType",
                createTransportTypeParams(volumeId, transportType));
    }

    private List<AccessProtocol> getAccessProtocolsOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetAccessProtocolsByGlusterVolumeGuid",
                accessProtocolRowMapper,
                createVolumeIdParams(volumeId));
    }

    private List<TransportType> getTransportTypesOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetTransportTypesByGlusterVolumeGuid",
                transportTypeRowMapper,
                createVolumeIdParams(volumeId));
    }

    private GlusterAsyncTask getAsyncTaskOfVolume(Guid volumeId) {
        List<GlusterAsyncTask> glusterAsyncTasks = getCallsHandler().executeReadList(
                "GetGlusterTaskByGlusterVolumeGuid",
                glusterAsyncTaskRowMapper,
                createVolumeIdParams(volumeId));

        if(glusterAsyncTasks != null && !glusterAsyncTasks.isEmpty()) {
            return glusterAsyncTasks.get(0);
        }
        return null;
    }

    private MapSqlParameterSource createVolumeIdParams(Guid id) {
        return getCustomMapSqlParameterSource().addValue("volume_id", id);
    }

    private MapSqlParameterSource createAccessProtocolParams(Guid volumeId, AccessProtocol protocol) {
        return createVolumeIdParams(volumeId).addValue("access_protocol", EnumUtils.nameOrNull(protocol));
    }

    private MapSqlParameterSource createTransportTypeParams(Guid volumeId, TransportType transportType) {
        return createVolumeIdParams(volumeId).addValue("transport_type", EnumUtils.nameOrNull(transportType));
    }

    private void insertVolumeEntity(GlusterVolumeEntity volume) {
        getCallsHandler().executeModification("InsertGlusterVolume", createFullParametersMapper(volume));
    }

    private void insertVolumeBricks(GlusterVolumeEntity volume) {
        List<GlusterBrickEntity> bricks = volume.getBricks();
        for (GlusterBrickEntity brick : bricks) {
            if (brick.getVolumeId() == null) {
                brick.setVolumeId(volume.getId());
            }
            glusterBrickDao.save(brick);
        }
    }

    private void insertVolumeOptions(GlusterVolumeEntity volume) {
        Collection<GlusterVolumeOptionEntity> options = volume.getOptions();
        for (GlusterVolumeOptionEntity option : options) {
            if (option.getVolumeId() == null) {
                option.setVolumeId(volume.getId());
            }
            glusterOptionDao.save(option);
        }
    }

    private void insertVolumeAccessProtocols(GlusterVolumeEntity volume) {
        for (AccessProtocol protocol : volume.getAccessProtocols()) {
            addAccessProtocol(volume.getId(), protocol);
        }
    }

    private void insertVolumeTransportTypes(GlusterVolumeEntity volume) {
        for (TransportType transportType : volume.getTransportTypes()) {
            addTransportType(volume.getId(), transportType);
        }
    }

    /**
     * Fetches and populates related entities like bricks, options, access protocols for the given volumes
     */
    private void fetchRelatedEntities(List<GlusterVolumeEntity> volumes) {
        volumes.forEach(this::fetchRelatedEntities);
    }

    /**
     * Fetches and populates related entities like bricks, options, access protocols for the given volume
     */
    private void fetchRelatedEntities(GlusterVolumeEntity volume) {
        if (volume != null) {
            volume.setOptions(glusterOptionDao.getOptionsOfVolume(volume.getId()));
            volume.setAccessProtocols(new HashSet<>(getAccessProtocolsOfVolume(volume.getId())));
            volume.setTransportTypes(new HashSet<>(getTransportTypesOfVolume(volume.getId())));
            GlusterVolumeAdvancedDetails advancedDetails = fetchAdvancedDatails(volume.getId());
            if (advancedDetails != null) {
                volume.setAdvancedDetails(advancedDetails);
            }
            GlusterAsyncTask asyncTask = getAsyncTaskOfVolume(volume.getId());
            if (asyncTask != null) {
                volume.setAsyncTask(asyncTask);
            }
            List<GlusterBrickEntity> bricks = glusterBrickDao.getBricksOfVolume(volume.getId());
            if (volume.getAsyncTask() != null && volume.getAsyncTask().getTaskId() != null) {
                bricks.stream()
                        .filter(brick -> brick.getAsyncTask() != null &&
                                            brick.getAsyncTask().getTaskId() != null &&
                                            brick.getAsyncTask().getTaskId().equals(volume.getAsyncTask().getTaskId()))
                        .forEach(brick -> brick.setAsyncTask(volume.getAsyncTask()));
            }
            volume.setBricks(bricks);
            GlusterVolumeSnapshotConfig config = glusterVolumeSnapshotConfigDao
                            .getConfigByVolumeIdAndName(volume.getClusterId(),
                                    volume.getId(),
                                    GlusterConstants.VOLUME_SNAPSHOT_MAX_HARD_LIMIT);
            if (config == null || StringUtils.isEmpty(config.getParamValue())) {
                config = glusterVolumeSnapshotConfigDao
                                .getConfigByClusterIdAndName(volume.getClusterId(),
                                        GlusterConstants.VOLUME_SNAPSHOT_MAX_HARD_LIMIT);
            }
            volume.setSnapMaxLimit(config != null ? Integer.parseInt(config.getParamValue()) : 0);
        }
    }

    private GlusterVolumeAdvancedDetails fetchAdvancedDatails(Guid volumeId) {
        return getCallsHandler().executeRead(
                "GetGlusterVolumeDetailsByID",
                glusterVolumesAdvancedDetailsRowMapper,
                createVolumeIdParams(volumeId));
    }

    private MapSqlParameterSource createReplicaCountParams(Guid volumeId, int replicaCount) {
        return createVolumeIdParams(volumeId).addValue("replica_count", replicaCount);
    }

    @Override
    public void updateReplicaCount(Guid volumeId, int replicaCount) {
        getCallsHandler().executeModification("UpdateReplicaCount", createReplicaCountParams(volumeId, replicaCount));
    }

    @Override
    public void updateGlusterVolume(GlusterVolumeEntity volume) {
        getCallsHandler().executeModification("UpdateGlusterVolume",
                getCustomMapSqlParameterSource()
                        .addValue("id", volume.getId())
                        .addValue("cluster_id", volume.getClusterId())
                        .addValue("vol_name", volume.getName())
                        .addValue("vol_type", EnumUtils.nameOrNull(volume.getVolumeType()))
                        .addValue("status", EnumUtils.nameOrNull(volume.getStatus()))
                        .addValue("replica_count", volume.getReplicaCount())
                        .addValue("stripe_count", volume.getStripeCount())
                        .addValue("disperse_count", volume.getDisperseCount())
                        .addValue("redundancy_count", volume.getRedundancyCount())
                        .addValue("is_arbiter", volume.getIsArbiter()));
    }

    @Override
    public void updateVolumeCapacityInfo(GlusterVolumeSizeInfo volumeCapacityInfo) {
        getCallsHandler().executeModification("UpdateGlusterVolumeDetails",
                createCapacityInfoParas(volumeCapacityInfo));
    }

    @Override
    public void addVolumeCapacityInfo(GlusterVolumeSizeInfo volumeCapacityInfo) {
        getCallsHandler().executeModification("InsertGlusterVolumeDetails",
                createCapacityInfoParas(volumeCapacityInfo));
    }

    private MapSqlParameterSource createCapacityInfoParas(GlusterVolumeSizeInfo volumeCapacityInfo) {
        return getCustomMapSqlParameterSource()
                .addValue("volume_id", volumeCapacityInfo.getVolumeId())
                .addValue("total_space", volumeCapacityInfo.getTotalSize())
                .addValue("used_space", volumeCapacityInfo.getUsedSize())
                .addValue("free_space", volumeCapacityInfo.getFreeSize())
                .addValue("confirmed_free_space", volumeCapacityInfo.getConfirmedFreeSize())
                .addValue("vdo_savings", volumeCapacityInfo.getVdoSavings());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterVolumeEntity volume) {
        return getCustomMapSqlParameterSource()
                .addValue("id", volume.getId())
                .addValue("cluster_id", volume.getClusterId())
                .addValue("vol_name", volume.getName())
                .addValue("vol_type", EnumUtils.nameOrNull(volume.getVolumeType()))
                .addValue("status", EnumUtils.nameOrNull(volume.getStatus()))
                .addValue("replica_count", volume.getReplicaCount())
                .addValue("stripe_count", volume.getStripeCount())
                .addValue("disperse_count", volume.getDisperseCount())
                .addValue("redundancy_count", volume.getRedundancyCount())
                .addValue("is_arbiter", volume.getIsArbiter());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return createVolumeIdParams(id);
    }

    @Override
    protected RowMapper<GlusterVolumeEntity> createEntityRowMapper() {
        return volumeRowMapper;
    }

    @Override
    public void addTransportTypes(Guid volumeId, Collection<TransportType> transportTypes) {
        for (TransportType transportType : transportTypes) {
            addTransportType(volumeId, transportType);
        }
    }

    @Override
    public void removeTransportTypes(Guid volumeId, Collection<TransportType> transportTypes) {
        for (TransportType transportType : transportTypes) {
            removeTransportType(volumeId, transportType);
        }
    }
}
