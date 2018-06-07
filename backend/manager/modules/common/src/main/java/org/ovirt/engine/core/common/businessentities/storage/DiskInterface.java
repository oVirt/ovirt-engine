package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;

public enum DiskInterface {

    IDE("ide"),
    VirtIO_SCSI("scsi"),
    VirtIO("virtio"),
    SPAPR_VSCSI("scsi"),
    SATA("sata");

    private String name;

    DiskInterface(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DiskInterface forName(String name) {
        return Arrays.stream(values()).filter(val -> val.name.equals(name)).findAny().orElse(null);
    }
}
