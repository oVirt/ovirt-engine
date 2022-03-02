package org.ovirt.engine.core.common.vdscommands;


import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class ScratchDiskInfo {

    // Created scratch disk image.
    private DiskImage disk;
    // Path to the scratch disk image after it was prepared.
    private String path;

    @SuppressWarnings("unused")
    private ScratchDiskInfo() {
    }

    public ScratchDiskInfo(DiskImage scratchDisk, String scratchDiskPath) {
        this.disk = scratchDisk;
        this.path = scratchDiskPath;
    }

    public DiskImage getDisk() {
        return disk;
    }

    public void setDisk(DiskImage disk) {
        this.disk = disk;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
