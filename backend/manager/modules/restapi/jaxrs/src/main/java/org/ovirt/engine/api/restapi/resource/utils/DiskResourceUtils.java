package org.ovirt.engine.api.restapi.resource.utils;

import org.ovirt.engine.api.model.Disk;

public class DiskResourceUtils {

    public static boolean isLunDisk(Disk disk) {
        return disk.isSetLunStorage() && disk.getLunStorage().isSetLogicalUnits() &&
                disk.getLunStorage().getLogicalUnits().isSetLogicalUnits();
    }
}
