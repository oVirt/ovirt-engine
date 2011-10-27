package org.ovirt.engine.core.dal.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.DiskImage;

public class DiskImageByDriveMappingComparator implements Comparator<DiskImage> {

    @Override
    public int compare(DiskImage o1, DiskImage o2) {
        String internalDriveMapping1 = o1.getinternal_drive_mapping();
        String internalDriveMapping2 = o2.getinternal_drive_mapping();
        return internalDriveMapping1.compareTo(internalDriveMapping2);
    }

}
