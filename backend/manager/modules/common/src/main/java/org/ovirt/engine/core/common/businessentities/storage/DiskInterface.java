package org.ovirt.engine.core.common.businessentities.storage;

import java.util.HashMap;
import java.util.Map;

public enum DiskInterface {

    IDE("ide"),
    VirtIO_SCSI("scsi"),
    VirtIO("virtio"),
    SPAPR_VSCSI("scsi");

    private String name;
    private static Map<String, DiskInterface> mappings;

    static {
        mappings = new HashMap<>();
        for (DiskInterface error : values()) {
            mappings.put(error.getName(), error);
        }
    }

    private DiskInterface(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DiskInterface forValue(String name) {
        return mappings.get(name);
    }
}
