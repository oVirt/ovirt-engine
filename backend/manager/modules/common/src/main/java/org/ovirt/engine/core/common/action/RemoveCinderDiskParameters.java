package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RemoveCinderDiskParameters extends RemoveImageParameters {

    private CinderDisk removedVolume;
    protected LinkedList<RemoveCinderDiskVolumeParameters> childCommandsParameters = new LinkedList<>();
    private List<Guid> finishedChildCmdIds = new ArrayList<>();
    private int removedVolumeIndex = 0;
    private Guid vmId;
    private Guid storageDomainId;
    private boolean updateSnapshot;

    public RemoveCinderDiskParameters() {
    }

    public RemoveCinderDiskParameters(Guid diskId) {
        // We use disk id instead of image id.
        super(diskId);
    }

    public CinderDisk getRemovedVolume() {
        return removedVolume;
    }

    public void setRemovedVolume(CinderDisk removedVolume) {
        this.removedVolume = removedVolume;
    }

    public Guid getDiskId() {
        return getImageId();
    }

    public LinkedList<RemoveCinderDiskVolumeParameters> getChildCommandsParameters() {
        return childCommandsParameters;
    }

    public void setChildCommandsParameters(LinkedList<RemoveCinderDiskVolumeParameters> childCommands) {
        this.childCommandsParameters = childCommands;
    }

    public List<Guid> getFinishedChildCmdIds() {
        return finishedChildCmdIds;
    }

    public void setFinishedChildCmdIds(List<Guid> finishedChildCmdIds) {
        this.finishedChildCmdIds = finishedChildCmdIds;
    }

    public int getRemovedVolumeIndex() {
        return removedVolumeIndex;
    }

    public void setRemovedVolumeIndex(int removedVolumeIndex) {
        this.removedVolumeIndex = removedVolumeIndex;
    }

    public boolean isUpdateSnapshot() {
        return updateSnapshot;
    }

    public void setUpdateSnapshot(boolean updateSnapshot) {
        this.updateSnapshot = updateSnapshot;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
