package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GlusterVolumeSnapshotCreateReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String NAME = "name";
    private static final String UUID = "uuid";

    private StatusForXmlRpc status;
    private GlusterVolumeSnapshotEntity snapshot;

    public GlusterVolumeSnapshotEntity getSnapshot() {
        return snapshot;
    }

    public GlusterVolumeSnapshotCreateReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        snapshot = prepareSnapshtoDetail(innerMap);
    }

    private GlusterVolumeSnapshotEntity prepareSnapshtoDetail(Map<String, Object> map) {
        GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setSnapshotId(Guid.createGuidFromString((String) map.get(UUID)));
        snapshot.setSnapshotName((String) map.get(NAME));
        snapshot.setCreatedAt(new Date());

        return snapshot;
    }

    public StatusForXmlRpc getStatus() {
        return status;
    }

    public void setStatus(StatusForXmlRpc status) {
        this.status = status;
    }
}
