package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CloneCinderDisksParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 1528721415797299722L;

    private List<CinderDisk> cinderDisks;
    private Guid vmSnapshotId;
    private Map<Guid, ? extends DiskImage> disksMap;
    private boolean parentHasTasks;

    public CloneCinderDisksParameters() {
    }

    public CloneCinderDisksParameters(List<CinderDisk> cinderDisks, Guid vmSnapshotId, Map<Guid, ? extends DiskImage> disksMap) {
        this.cinderDisks = cinderDisks;
        this.vmSnapshotId = vmSnapshotId;
        this.disksMap = disksMap;
    }

    public List<CinderDisk> getCinderDisks() {
        return cinderDisks;
    }

    public void setCinderDisks(List<CinderDisk> cinderDisks) {
        this.cinderDisks = cinderDisks;
    }

    public Guid getVmSnapshotId() {
        return vmSnapshotId;
    }

    public void setVmSnapshotId(Guid vmSnapshotId) {
        this.vmSnapshotId = vmSnapshotId;
    }

    public Map<Guid, ? extends DiskImage> getDisksMap() {
        return disksMap;
    }

    public void setDisksMap(Map<Guid, ? extends DiskImage> disksMap) {
        this.disksMap = disksMap;
    }

    public boolean isParentHasTasks() {
        return parentHasTasks;
    }

    public void setParentHasTasks(boolean parentHasTasks) {
        this.parentHasTasks = parentHasTasks;
    }
}
