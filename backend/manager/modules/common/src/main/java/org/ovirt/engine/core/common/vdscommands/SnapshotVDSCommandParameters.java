package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class SnapshotVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    /** The disks images that snapshot should be taken for */
    private List<DiskImage> images;

    private DiskImage memoryDump;
    private DiskImage memoryConf;

    /** A flag to indicate whether the VM has been frozen **/
    private boolean vmFrozen;

    /** Live snapshot timeout for memory or non-memory **/
    private int timeout;

    public SnapshotVDSCommandParameters(Guid vdsId, Guid vmId, List<DiskImage> images) {
        super(vdsId, vmId);
        this.images = images;
    }

    public SnapshotVDSCommandParameters() {
    }

    public List<DiskImage> getImages() {
        return images;
    }


    public boolean isMemoryVolumeExists() {
        return memoryDump != null || memoryConf != null;
    }

    public boolean isVmFrozen() {
        return vmFrozen;
    }

    public void setVmFrozen(boolean vmFrozen) {
        this.vmFrozen = vmFrozen;
    }

    public DiskImage getMemoryDump() {
        return memoryDump;
    }

    public void setMemoryDump(DiskImage memoryDump) {
        this.memoryDump = memoryDump;
    }

    public DiskImage getMemoryConf() {
        return memoryConf;
    }

    public void setMemoryConf(DiskImage memoryConf) {
        this.memoryConf = memoryConf;
    }

    public int getLiveSnapshotTimeout() {
        return timeout;
    }

    public void setLiveSnapshotTimeout(int timeout) {
        this.timeout = timeout;
    }

}
