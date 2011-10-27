package org.ovirt.engine.core.dal.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.DiskImage;

public class DiskImageByBootComparator implements Comparator<DiskImage> {

    @Override
    public int compare(DiskImage o1, DiskImage o2) {
        Boolean boot1 = o1.getboot();
        Boolean boot2 = o2.getboot();
        return boot1.compareTo(boot2);
    }
}
