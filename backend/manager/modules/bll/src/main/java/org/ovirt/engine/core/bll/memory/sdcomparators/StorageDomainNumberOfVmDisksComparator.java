package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainNumberOfVmDisksComparator implements Comparator<StorageDomain> {

    private Map<Guid, Long> numOfVmDisksInStorageDomains;

    public StorageDomainNumberOfVmDisksComparator(Collection<DiskImage> vmDisks) {
        numOfVmDisksInStorageDomains = vmDisks.stream().collect(Collectors.groupingBy(
                vmDisk -> vmDisk.getStorageIds().get(0), Collectors.counting()
        ));
    }

    @Override
    public int compare(StorageDomain storageDomain, StorageDomain storageDomain2) {
        Long numOfVmDisksInStorageDomain = numOfVmDisksInStorageDomains.getOrDefault(storageDomain.getId(), 0L);
        Long numOfVmDisksInStorageDomain2 = numOfVmDisksInStorageDomains.getOrDefault(storageDomain2.getId(), 0L);

        // Prefer the domain with more disks on it
        return -1 * numOfVmDisksInStorageDomain.compareTo(numOfVmDisksInStorageDomain2);
    }
}
