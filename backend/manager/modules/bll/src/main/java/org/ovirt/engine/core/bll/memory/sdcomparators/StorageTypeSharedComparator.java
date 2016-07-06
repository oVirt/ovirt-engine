package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.StorageDomain;

public class StorageTypeSharedComparator implements Comparator<StorageDomain> {

    @Override
    public int compare(StorageDomain storageDomain, StorageDomain storageDomain2) {
        return Boolean.compare(storageDomain.isShared(), storageDomain2.isShared());
    }
}
