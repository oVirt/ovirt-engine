package org.ovirt.engine.core.vdsbroker.gluster;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public final class GlusterVolumeSnapshotInfoReturn extends StatusReturn {
    private static final String STATUS = "status";
    private static final String SNAPSHOT_LIST = "snapshotList";
    private static final String SNAPSHOTS = "snapshots";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String CREATETIME = "createTime";
    private static final String EPOCH_TIME = "epochTime";
    private static final String SNAPSHOT_ID = "id";
    private static final String SNAP_VOLUME_STATUS = "snapVolumeStatus";

    private Status status;
    private static final Logger log = LoggerFactory.getLogger(GlusterVolumesListReturn.class);
    private List<GlusterVolumeSnapshotEntity> glusterVolumeSnapshots = new ArrayList<>();

    public List<GlusterVolumeSnapshotEntity> getSnapshots() {
        return glusterVolumeSnapshots;
    }

    public GlusterVolumeSnapshotInfoReturn(Guid clusterId, Map<String, Object> innerMap) {
        super(innerMap);
        status = new Status((Map<String, Object>) innerMap.get(STATUS));

        Map<String, Object> snapshots = (Map<String, Object>) innerMap.get(SNAPSHOT_LIST);

        glusterVolumeSnapshots = prepareVolumeSnapshotsList(clusterId, snapshots);
    }

    private List<GlusterVolumeSnapshotEntity> prepareVolumeSnapshotsList(Guid clusterId, Map<String, Object> snapshots) {

        List<GlusterVolumeSnapshotEntity> newSnapshotsList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : snapshots.entrySet()) {
            String volumeName = entry.getKey();
            Map<String, Object> snapshotInfo = (Map<String, Object>) entry.getValue();

            Object[] volumeSnapshots = (Object[]) snapshotInfo.get(SNAPSHOTS);
            GlusterVolumeEntity volumeEntity = getGlusterVolumeDao().getByName(clusterId, volumeName);

            for (Object snapshot : volumeSnapshots) {
                Map<String, Object> individualSnapshot = (Map<String, Object>) snapshot;
                GlusterVolumeSnapshotEntity newSnapshot = new GlusterVolumeSnapshotEntity();
                newSnapshot.setClusterId(clusterId);
                newSnapshot.setVolumeId(volumeEntity.getId());
                newSnapshot.setSnapshotId(Guid.createGuidFromString((String) individualSnapshot.get(SNAPSHOT_ID)));
                newSnapshot.setSnapshotName((String) individualSnapshot.get(NAME));
                newSnapshot.setDescription((String) individualSnapshot.get(DESCRIPTION));
                newSnapshot.setStatus(GlusterSnapshotStatus.from((String) individualSnapshot.get(SNAP_VOLUME_STATUS)));
                try {
                    Map<String, Object> createTimeDetail = (Map<String, Object>) individualSnapshot.get(CREATETIME);
                    long millis = (Integer) createTimeDetail.get(EPOCH_TIME) * 1000L;
                    Date createDate = new Date(millis);
                    // Convert to UTC
                    DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                    String formattedCreateDate = format.format(createDate);
                    newSnapshot.setCreatedAt(new Date(formattedCreateDate));
                } catch (Exception e) {
                    log.info("Could not populate creation time for snapshot '{}' of volume '{}' on cluster '{}': {}",
                            snapshotInfo.get(NAME),
                            volumeEntity.getName(),
                            clusterId,
                            e.getMessage());
                    log.debug("Exception", e);
                }
                newSnapshotsList.add(newSnapshot);
            }
        }

        return newSnapshotsList;
    }

    private GlusterVolumeDao getGlusterVolumeDao() {
        return Injector.get(GlusterVolumeDao.class);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
