package org.ovirt.engine.core.dal.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Disk;

public class DiskImageByBootComparator implements Comparator<Disk>, Serializable {
    private static final long serialVersionUID = 4732164571328497830L;

    @Override
    public int compare(Disk o1, Disk o2) {
        Boolean boot1 = o1.isBoot();
        Boolean boot2 = o2.isBoot();
        return boot1.compareTo(boot2);
    }
}
