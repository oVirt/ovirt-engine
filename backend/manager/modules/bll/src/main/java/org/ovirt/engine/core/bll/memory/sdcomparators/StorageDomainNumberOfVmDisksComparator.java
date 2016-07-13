package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainNumberOfVmDisksComparator implements Comparator<StorageDomain> {

    private Map<Guid, Integer> numOfVmDisksInStorageDomains;

    public StorageDomainNumberOfVmDisksComparator(Collection<DiskImage> vmDisks) {
        setUpNumOfVmDisksInStorageDomains(vmDisks);
    }

    @Override
    public int compare(StorageDomain storageDomain, StorageDomain storageDomain2) {
        Integer numOfVmDisksInStorageDomain = numOfVmDisksInStorageDomains.getOrDefault(storageDomain.getId(), 0);
        Integer numOfVmDisksInStorageDomain2 = numOfVmDisksInStorageDomains.getOrDefault(storageDomain2.getId(), 0);
        return numOfVmDisksInStorageDomain.compareTo(numOfVmDisksInStorageDomain2);
    }

    private void setUpNumOfVmDisksInStorageDomains(Collection<DiskImage> vmDisks) {
        numOfVmDisksInStorageDomains = new HashMap<>();

        for (DiskImage vmDisk : vmDisks) {
            Guid vmDiskStorageDomainId = vmDisk.getStorageIds().get(0);
            numOfVmDisksInStorageDomains.put(vmDiskStorageDomainId,
                    numOfVmDisksInStorageDomains.getOrDefault(vmDiskStorageDomainId, 0) + 1);
        }
    }
}
