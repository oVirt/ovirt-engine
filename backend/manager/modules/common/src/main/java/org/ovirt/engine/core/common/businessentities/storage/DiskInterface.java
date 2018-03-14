package org.ovirt.engine.core.common.businessentities.storage;

public enum DiskInterface {

    IDE("ide"),
    VirtIO_SCSI("scsi"),
    VirtIO("virtio"),
    SPAPR_VSCSI("scsi"),
    SATA("sata");

    private String name;

    private DiskInterface(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
