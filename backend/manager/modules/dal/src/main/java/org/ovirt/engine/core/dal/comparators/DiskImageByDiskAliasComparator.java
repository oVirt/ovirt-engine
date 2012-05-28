package org.ovirt.engine.core.dal.comparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Disk;

public class DiskImageByDiskAliasComparator implements Comparator<Disk> {

    @Override
    public int compare(Disk o1, Disk o2) {
        return o1.getDiskAlias().compareTo(o2.getDiskAlias());
    }

}
