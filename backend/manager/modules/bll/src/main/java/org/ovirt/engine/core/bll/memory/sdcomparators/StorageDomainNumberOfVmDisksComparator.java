package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainNumberOfVmDisksComparator implements Comparator<StorageDomain> {

    private Map<Guid, Integer> numOfVmDisksInStorageDomains;

    public StorageDomainNumberOfVmDisksComparator(List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        setUpNumOfVmDisksInStorageDomains(domainsInPool, vmDisks);
    }

    @Override
    public int compare(StorageDomain storageDomain, StorageDomain storageDomain2) {
        Integer numOfVmDisksInStorageDomain = numOfVmDisksInStorageDomains.get(storageDomain.getId());
        Integer numOfVmDisksInStorageDomain2 = numOfVmDisksInStorageDomains.get(storageDomain2.getId());
        return numOfVmDisksInStorageDomain.compareTo(numOfVmDisksInStorageDomain2);
    }

    private void setUpNumOfVmDisksInStorageDomains(List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        numOfVmDisksInStorageDomains = new HashMap<>();

        // Initialize each storage domain to have 0 disks.
        for (StorageDomain storageDomain : domainsInPool) {
            numOfVmDisksInStorageDomains.put(storageDomain.getId(), 0);
        }

        for (DiskImage vmDisk : vmDisks) {
            Guid vmDiskStorageDomainId = vmDisk.getStorageIds().get(0);
            if (numOfVmDisksInStorageDomains.containsKey(vmDiskStorageDomainId)) {
                // The current vmDisk belongs to a storage domain which was not filtered by MemoryStorageHandler.
                numOfVmDisksInStorageDomains.put(vmDiskStorageDomainId,
                        numOfVmDisksInStorageDomains.get(vmDiskStorageDomainId) + 1);
            }
        }
    }
}
