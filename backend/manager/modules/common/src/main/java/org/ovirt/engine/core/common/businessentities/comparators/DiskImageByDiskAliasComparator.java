package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.storage.Disk;


public class DiskImageByDiskAliasComparator implements Comparator<Disk>, Serializable {
    private static final long serialVersionUID = 4948753716816170325L;

    @Override
    public int compare(Disk o1, Disk o2) {
        return o1.getDiskAlias().compareTo(o2.getDiskAlias());
    }

}
