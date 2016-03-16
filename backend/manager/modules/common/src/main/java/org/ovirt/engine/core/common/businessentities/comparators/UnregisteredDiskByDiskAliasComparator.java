package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;

public class UnregisteredDiskByDiskAliasComparator implements Comparator<UnregisteredDisk>, Serializable {
    private static final long serialVersionUID = 7644973658749395177L;

    @Override
    public int compare(UnregisteredDisk o1, UnregisteredDisk o2) {
        return o1.getDiskAlias().compareTo(o2.getDiskAlias());
    }
}
