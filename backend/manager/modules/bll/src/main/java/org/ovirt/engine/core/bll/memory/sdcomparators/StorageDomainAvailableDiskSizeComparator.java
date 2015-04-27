package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.StorageDomain;

public class StorageDomainAvailableDiskSizeComparator implements Comparator<StorageDomain> {

    @Override
    public int compare(StorageDomain storageDomain, StorageDomain storageDomain2) {
        return storageDomain.getAvailableDiskSize().compareTo(storageDomain2.getAvailableDiskSize());
    }
}
