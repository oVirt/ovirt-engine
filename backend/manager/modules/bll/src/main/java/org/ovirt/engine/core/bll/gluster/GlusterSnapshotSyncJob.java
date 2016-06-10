package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotConfigInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterSnapshotSyncJob extends GlusterJob {
    private static final Logger log = LoggerFactory.getLogger(GlusterSnapshotSyncJob.class);
    private static final GlusterSnapshotSyncJob instance = new GlusterSnapshotSyncJob();

    public void init() {
        log.info("Gluster snapshot monitoring has been initialized");
    }

    public static GlusterSnapshotSyncJob getInstance() {
        return instance;
    }

    @OnTimerMethodAnnotation("gluster_snapshot_poll_event")
    public void refreshSnapshotData() {
        refreshSnapshotList();
        refreshSnapshotConfig();
    }

    public void refreshSnapshotList() {
        // get all clusters
        List<Cluster> clusters = getClusterDao().getAll();

        for (Cluster cluster : clusters) {
            refreshSnapshotsInCluster(cluster);
        }
    }

    public void refreshSnapshotConfig() {
        // get all clusters
        List<Cluster> clusters = getClusterDao().getAll();

        for (Cluster cluster : clusters) {
            refreshSnapshotConfigInCluster(cluster);
        }
    }

    private void refreshSnapshotsInCluster(Cluster cluster) {
        if (!supportsGlusterSnapshotFeature(cluster)) {
            return;
        }

        final VDS upServer = getGlusterUtil().getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.info("No UP server found in cluster '{}' for snapshot monitoring", cluster.getName());
            return;
        }

        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeSnapshotInfo,
                new GlusterVolumeSnapshotVDSParameters(upServer.getId(), cluster.getId(), null));
        if (returnValue.getSucceeded()) {
            addOrUpdateSnapshots(cluster.getId(), (ArrayList<GlusterVolumeSnapshotEntity>) returnValue.getReturnValue());

            // check if the snapshot soft limit reached for a volume and alert
            List<GlusterVolumeEntity> volumes = getGlusterVolumeDao().getByClusterId(cluster.getId());
            for (final GlusterVolumeEntity volume : volumes) {
                // check if the snapshot soft limit reached for the volume and alert
                getGlusterUtil().alertVolumeSnapshotLimitsReached(volume);

                // Check and remove soft limit alert for the volume.
                // It might have fallen below the soft limit as part of deletions of snapshots
                getGlusterUtil().checkAndRemoveVolumeSnapshotLimitsAlert(volume);
            }
        } else {
            log.error("VDS Error {}", returnValue.getVdsError().getMessage());
            log.debug("VDS Error {}", returnValue.getVdsError());
        }
    }

    public void refreshSnapshotConfigInCluster(Cluster cluster) {
        if (!supportsGlusterSnapshotFeature(cluster)) {
            return;
        }

        final VDS upServer = getGlusterUtil().getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.info("No UP server found in cluster '{}' for snapshot configurations monitoring", cluster.getName());
            return;
        }

        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeSnapshotConfigInfo,
                        new GlusterVolumeSnapshotVDSParameters(upServer.getId(), cluster.getId(), null));
        if (returnValue.getSucceeded()) {
            addOrUpdateSnapshotsConfig(cluster.getId(), (GlusterSnapshotConfigInfo) returnValue.getReturnValue());
        } else {
            log.error("VDS Error {}", returnValue.getVdsError().getMessage());
            log.debug("VDS Error {}", returnValue.getVdsError());
        }
    }

    private void addOrUpdateSnapshots(Guid clusterId, List<GlusterVolumeSnapshotEntity> fetchedSnapshots) {
        Map<Guid, GlusterVolumeSnapshotEntity> fetchedSnapshotsMap = new HashMap<>();
        for (GlusterVolumeSnapshotEntity fetchedSnapshot : fetchedSnapshots) {
            fetchedSnapshotsMap.put(fetchedSnapshot.getId(), fetchedSnapshot);
        }

        Cluster cluster = getClusterDao().get(clusterId);
        List<GlusterVolumeSnapshotEntity> existingSnapshots =
                getGlusterVolumeSnapshotDao().getAllByClusterId(clusterId);
        Map<Guid, GlusterVolumeSnapshotEntity> existingSnapshotsMap = new HashMap<>();
        for (GlusterVolumeSnapshotEntity existingSnapshot : existingSnapshots) {
            existingSnapshotsMap.put(existingSnapshot.getId(), existingSnapshot);
        }

        List<GlusterVolumeSnapshotEntity> updatedSnapshots = new ArrayList<>();
        List<GlusterVolumeSnapshotEntity> newlyAddedSnapshots = new ArrayList<>();
        List<GlusterVolumeSnapshotEntity> deletedSnapshots = new ArrayList<>();

        for (final GlusterVolumeSnapshotEntity fetchedSnapshot : fetchedSnapshots) {
            GlusterVolumeSnapshotEntity correspondingExistingSnapshot =
                    existingSnapshotsMap.get(fetchedSnapshot.getId());
            if (correspondingExistingSnapshot == null) {
                final GlusterVolumeEntity volume = getGlusterVolumeDao().getById(fetchedSnapshot.getVolumeId());
                newlyAddedSnapshots.add(fetchedSnapshot);
                log.debug("Detected new gluster volume snapshot '{}' for volume '{}' on cluster: '{}'",
                        fetchedSnapshot.getSnapshotName(),
                        volume.getName(),
                        cluster.getName());
                logUtil.logAuditMessage(clusterId,
                        volume,
                        null,
                        AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DETECTED_NEW,
                        new HashMap<String, String>() {
                            {
                                put("snapName", fetchedSnapshot.getSnapshotName());
                                put(GlusterConstants.VOLUME_NAME, volume.getName());
                            }
                        });
            } else if (correspondingExistingSnapshot.getStatus() != fetchedSnapshot.getStatus()) {
                correspondingExistingSnapshot.setStatus(fetchedSnapshot.getStatus());
                updatedSnapshots.add(correspondingExistingSnapshot);
            }
        }

        for (final GlusterVolumeSnapshotEntity existingSnapshot : existingSnapshots) {
            GlusterVolumeSnapshotEntity correspondingFetchedSnapshot =
                    fetchedSnapshotsMap.get(existingSnapshot.getId());
            if (correspondingFetchedSnapshot == null) {
                final GlusterVolumeEntity volume = getGlusterVolumeDao().getById(existingSnapshot.getVolumeId());
                deletedSnapshots.add(existingSnapshot);
                log.debug("Gluster volume snapshot '{}' detected removed for volume '{}' on cluster: '{}'",
                        existingSnapshot.getSnapshotName(),
                        volume.getName(),
                        cluster.getName());
                logUtil.logAuditMessage(clusterId,
                        volume,
                        null,
                        AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETED_FROM_CLI,
                        new HashMap<String, String>() {
                            {
                                put("snapName", existingSnapshot.getSnapshotName());
                                put(GlusterConstants.VOLUME_NAME, volume.getName());
                            }
                        });
            }
        }

        // update snapshot details
        try (EngineLock lock = acquireVolumeSnapshotLock(clusterId)) {
            saveNewSnapshots(newlyAddedSnapshots);
            updateSnapshots(updatedSnapshots);
            deleteSnapshots(deletedSnapshots);
        } catch (Exception e) {
            log.error("Exception ocuured while adding/updating snapshots from CLI - '{}'", e.getMessage());
            log.debug("Exception", e);
            throw new EngineException(EngineError.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
        }
    }

    private void addOrUpdateSnapshotsConfig(Guid clusterId, GlusterSnapshotConfigInfo configInfo) {
        Cluster cluster = getClusterDao().get(clusterId);
        try (EngineLock lock = acquireVolumeSnapshotLock(clusterId)) {
            for (Map.Entry<String, String> entry : configInfo.getClusterConfigOptions().entrySet()) {
                if (entry.getValue() != null) {
                    addOrUpdateClusterConfig(cluster, entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            log.error("Exception ocuured while adding/updating snapshots configurations from CLI - '{}'",
                    e.getMessage());
            log.debug("Exception", e);
            throw new EngineException(EngineError.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
        }

        Map<String, Map<String, String>> volumeConfigs = configInfo.getVolumeConfigOptions();
        for (Map.Entry<String, Map<String, String>> entry : volumeConfigs.entrySet()) {
            GlusterVolumeEntity volume = getGlusterVolumeDao().getByName(clusterId, entry.getKey());
            if (volume == null) {
                continue;
            }

            try (EngineLock lock = acquireVolumeSnapshotLock(volume.getId())) {
                Map<String, String> volumeConfig = entry.getValue();
                if (volumeConfig != null) {
                    for (Map.Entry<String, String> entry1 : volumeConfig.entrySet()) {
                        if (entry.getValue() != null) {
                            addOrUpdateVolumeConfig(cluster,
                                    volume,
                                    entry1.getKey(),
                                    entry1.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception ocuured while adding/updating snapshots configurations from CLI - '{}'",
                        e.getMessage());
                log.debug("Exception", e);
                throw new EngineException(EngineError.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
            }
        }
    }

    private void addOrUpdateClusterConfig(Cluster cluster, final String paramName, final String paramValue) {
        GlusterVolumeSnapshotConfig param = new GlusterVolumeSnapshotConfig();
        param.setClusterId(cluster.getId());
        param.setVolumeId(null);
        param.setParamName(paramName);
        param.setParamValue(paramValue);
        GlusterVolumeSnapshotConfig existingParamDetail =
                getGlusterVolumeSnapshotConfigDao().getConfigByClusterIdAndName(cluster.getId(),
                        paramName);
        if (existingParamDetail == null) {
            getGlusterVolumeSnapshotConfigDao().save(param);
            log.debug("Detected new gluster volume snapshot configuration '{}' with value '{}' for cluster: '{}'",
                    paramName,
                    paramValue,
                    cluster.getName());
            logUtil.logAuditMessage(cluster.getId(),
                    null,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CLUSTER_CONFIG_DETECTED_NEW,
                    new HashMap<String, String>() {
                        {
                            put("snapConfigName", paramName);
                            put("snapConfigValue", paramValue);
                        }
                    });
        } else if (!existingParamDetail.getParamValue().equals(paramValue)) {
            getGlusterVolumeSnapshotConfigDao().updateConfigByClusterIdAndName(cluster.getId(),
                    paramName,
                    paramValue);
        }
    }

    private void addOrUpdateVolumeConfig(Cluster cluster,
            final GlusterVolumeEntity volume,
            final String paramName,
            final String paramValue) {
        GlusterVolumeSnapshotConfig cfg = new GlusterVolumeSnapshotConfig();
        cfg.setClusterId(cluster.getId());
        cfg.setVolumeId(volume.getId());
        cfg.setParamName(paramName);
        cfg.setParamValue(paramValue);
        GlusterVolumeSnapshotConfig existingParamDetail =
                getGlusterVolumeSnapshotConfigDao().getConfigByVolumeIdAndName(cluster.getId(),
                        volume.getId(),
                        paramName);
        if (existingParamDetail == null) {
            getGlusterVolumeSnapshotConfigDao().save(cfg);
            log.debug("Detected new gluster volume snapshot configuration '{}' with value '{}' for volume: '{}' on cluster '{}'",
                    paramName,
                    paramValue,
                    cluster.getName(),
                    volume.getName());
            logUtil.logAuditMessage(cluster.getId(),
                    volume,
                    null,
                    AuditLogType.GLUSTER_VOLUME_SNAPSHOT_VOLUME_CONFIG_DETECTED_NEW,
                    new HashMap<String, String>() {
                        {
                            put("snapConfigName", paramName);
                            put("snapConfigValue", paramValue);
                            put(GlusterConstants.VOLUME_NAME, volume.getName());
                        }
                    });
        } else if (!existingParamDetail.getParamValue().equals(paramValue)) {
            getGlusterVolumeSnapshotConfigDao().updateConfigByVolumeIdAndName(cluster.getId(),
                    volume.getId(),
                    paramName,
                    paramValue);
        }
    }

    private void saveNewSnapshots(List<GlusterVolumeSnapshotEntity> snapshots) {
        getGlusterVolumeSnapshotDao().saveAll(snapshots);
    }

    private void updateSnapshots(List<GlusterVolumeSnapshotEntity> snapshots) {
        getGlusterVolumeSnapshotDao().updateAllInBatch(snapshots);
    }

    private void deleteSnapshots(List<GlusterVolumeSnapshotEntity> snaphosts) {
        List<Guid> deletedIds = new ArrayList<>();
        for (GlusterVolumeSnapshotEntity snapshot : snaphosts) {
            deletedIds.add(snapshot.getId());
        }
        getGlusterVolumeSnapshotDao().removeAll(deletedIds);
    }

    private boolean supportsGlusterSnapshotFeature(Cluster cluster) {
        return cluster.supportsGlusterService();
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance().getGlusterVolumeDao();
    }

    protected GlusterVolumeSnapshotDao getGlusterVolumeSnapshotDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotDao();
    }

    protected GlusterVolumeSnapshotConfigDao getGlusterVolumeSnapshotConfigDao() {
        return DbFacade.getInstance().getGlusterVolumeSnapshotConfigDao();
    }
}
