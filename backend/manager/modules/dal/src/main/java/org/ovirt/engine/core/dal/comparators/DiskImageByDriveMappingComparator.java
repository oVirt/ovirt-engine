package org.ovirt.engine.core.dal.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Disk;

public class DiskImageByDriveMappingComparator implements Comparator<Disk> {

    @Override
    public int compare(Disk o1, Disk o2) {
        return o1.getInternalDriveMapping() - o2.getInternalDriveMapping();
    }

}
