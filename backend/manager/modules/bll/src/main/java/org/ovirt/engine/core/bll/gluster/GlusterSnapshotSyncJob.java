package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotConfigInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
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
        List<VDSGroup> clusters = getClusterDao().getAll();

        for (VDSGroup cluster : clusters) {
            refreshSnapshotsInCluster(cluster);
        }
    }

    public void refreshSnapshotConfig() {
        // get all clusters
        List<VDSGroup> clusters = getClusterDao().getAll();

        for (VDSGroup cluster : clusters) {
            refreshSnapshotConfigInCluster(cluster);
        }
    }

    private void refreshSnapshotsInCluster(VDSGroup cluster) {
        if (!supportsGlusterSnapshotFeature(cluster)) {
            return;
        }

        final VDS upServer = getClusterUtils().getRandomUpServer(cluster.getId());
        if (upServer == null) {
            log.info("No UP server found in cluster '{}' for snapshot monitoring", cluster.getName());
            return;
        }

        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeSnapshotInfo,
                new GlusterVolumeSnapshotVDSParameters(upServer.getId(), cluster.getId(), null));
        if (returnValue.getSucceeded()) {
            addOrUpdateSnapshots(cluster.getId(), (ArrayList<GlusterVolumeSnapshotEntity>) returnValue.getReturnValue());
        } else {
            log.error("VDS Error {}", returnValue.getVdsError().getMessage());
            log.debug("VDS Error {}", returnValue.getVdsError());
        }
    }

    public void refreshSnapshotConfigInCluster(VDSGroup cluster) {
        if (!supportsGlusterSnapshotFeature(cluster)) {
            return;
        }

        final VDS upServer = getClusterUtils().getRandomUpServer(cluster.getId());
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

        List<GlusterVolumeSnapshotEntity> existingSnapshots =
                getGlusterVolumeSnapshotDao().getAllByClusterId(clusterId);
        Map<Guid, GlusterVolumeSnapshotEntity> existingSnapshotsMap = new HashMap<>();
        for (GlusterVolumeSnapshotEntity existingSnapshot : existingSnapshots) {
            existingSnapshotsMap.put(existingSnapshot.getId(), existingSnapshot);
        }

        List<GlusterVolumeSnapshotEntity> updatedSnapshots = new ArrayList<>();
        List<GlusterVolumeSnapshotEntity> newlyAddedSnapshots = new ArrayList<>();
        List<GlusterVolumeSnapshotEntity> deletedSnapshots = new ArrayList<>();

        for (GlusterVolumeSnapshotEntity fetchedSnapshot : fetchedSnapshots) {
            GlusterVolumeSnapshotEntity correspondingExistingSnapshot =
                    existingSnapshotsMap.get(fetchedSnapshot.getId());
            if (correspondingExistingSnapshot == null) {
                newlyAddedSnapshots.add(fetchedSnapshot);
            } else if (correspondingExistingSnapshot.getStatus() != fetchedSnapshot.getStatus()) {
                correspondingExistingSnapshot.setStatus(fetchedSnapshot.getStatus());
                updatedSnapshots.add(correspondingExistingSnapshot);
            }
        }

        for (GlusterVolumeSnapshotEntity existingSnapshot : existingSnapshots) {
            GlusterVolumeSnapshotEntity correspondingFetchedSnapshot =
                    fetchedSnapshotsMap.get(existingSnapshot.getId());
            if (correspondingFetchedSnapshot == null) {
                deletedSnapshots.add(existingSnapshot);
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
            throw new VdcBLLException(VdcBllErrors.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
        }
    }

    private void addOrUpdateSnapshotsConfig(Guid clusterId, GlusterSnapshotConfigInfo configInfo) {
        try (EngineLock lock = acquireVolumeSnapshotLock(clusterId)) {
            for (Map.Entry<String, String> entry : configInfo.getClusterConfigOptions().entrySet()) {
                if (entry.getValue() != null) {
                    addOrUpdateClusterConfig(clusterId, entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            log.error("Exception ocuured while adding/updating snapshots configurations from CLI - '{}'",
                    e.getMessage());
            log.debug("Exception", e);
            throw new VdcBLLException(VdcBllErrors.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
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
                            addOrUpdateVolumeConfig(clusterId,
                                    volume.getId(),
                                    entry1.getKey(),
                                    entry1.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception ocuured while adding/updating snapshots configurations from CLI - '{}'",
                        e.getMessage());
                log.debug("Exception", e);
                throw new VdcBLLException(VdcBllErrors.GlusterSnapshotInfoFailedException, e.getLocalizedMessage());
            }
        }
    }

    private void addOrUpdateClusterConfig(Guid clusterId, String paramName, String paramValue) {
        GlusterVolumeSnapshotConfig param = new GlusterVolumeSnapshotConfig();
        param.setClusterId(clusterId);
        param.setVolumeId(null);
        param.setParamName(paramName);
        param.setParamValue(paramValue);
        GlusterVolumeSnapshotConfig existingParamDetail =
                getGlusterVolumeSnapshotConfigDao().getConfigByClusterIdAndName(clusterId,
                        paramName);
        if (existingParamDetail == null) {
            getGlusterVolumeSnapshotConfigDao().save(param);
        } else if (!(existingParamDetail.getParamValue().equals(paramValue))) {
            getGlusterVolumeSnapshotConfigDao().updateConfigByClusterIdAndName(clusterId,
                    paramName,
                    paramValue);
        }
    }

    private void addOrUpdateVolumeConfig(Guid clusterId, Guid volumeId, String paramName, String paramValue) {
        GlusterVolumeSnapshotConfig cfg = new GlusterVolumeSnapshotConfig();
        cfg.setClusterId(clusterId);
        cfg.setVolumeId(volumeId);
        cfg.setParamName(paramName);
        cfg.setParamValue(paramValue);
        GlusterVolumeSnapshotConfig existingParamDetail =
                getGlusterVolumeSnapshotConfigDao().getConfigByVolumeIdAndName(clusterId,
                        volumeId,
                        paramName);
        if (existingParamDetail == null) {
            getGlusterVolumeSnapshotConfigDao().save(cfg);
        } else if (!(existingParamDetail.getParamValue().equals(paramValue))) {
            getGlusterVolumeSnapshotConfigDao().updateConfigByVolumeIdAndName(clusterId,
                    volumeId,
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

    private boolean supportsGlusterSnapshotFeature(VDSGroup cluster) {
        return cluster.supportsGlusterService()
                && GlusterFeatureSupported.glusterSnapshot(cluster.getCompatibilityVersion());
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
