package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.bll.memory.sdcomparators.StorageDomainAvailableDiskSizeComparator;
import org.ovirt.engine.core.bll.memory.sdcomparators.StorageDomainNumberOfVmDisksComparator;
import org.ovirt.engine.core.bll.memory.sdcomparators.StorageTypeComparator;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainSpaceRequirementsFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainStatusFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainTypeFilter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryStorageHandler {

    private static final MemoryStorageHandler instance = new MemoryStorageHandler();

    private static final Logger log = LoggerFactory.getLogger(MemoryStorageHandler.class);

    private MemoryStorageHandler() {
    }

    public static MemoryStorageHandler getInstance() {
        return instance;
    }

    /**
     * Returns a <code>StorageDomain</code> in the given <code>StoragePool</code> that has
     * at least as much as requested free space and can be used to store memory images
     *
     * @param storagePoolId
     *           The storage pool where the search for a domain will be made
     * @param memoryDisks
     *           Disks for which space is needed
     * @param vmDisks
     *           The vm's active snapshot disks.
     * @param vmForLogging
     *           The VM which the memory volumes being created belong to.<br/>
     *           Note: This parameter is used for logging purposed only.
     * @return storage domain in the given pool with at least the required amount of free space,
     *         or null if no such storage domain exists in the pool
     */
    public StorageDomain findStorageDomainForMemory(Guid storagePoolId, List<DiskImage> memoryDisks,
            Collection<DiskImage> vmDisks, VM vmForLogging) {
        List<StorageDomain> domainsInPool =
                DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(storagePoolId);
        StorageDomain storageDomainForMemory = findStorageDomainForMemory(domainsInPool, memoryDisks, vmDisks);
        if (storageDomainForMemory != null) {
            updateDisksStorage(storageDomainForMemory, memoryDisks);
            if (vmForLogging != null) {
                log.info("The memory volumes of VM (name '{}', id '{}') will be " +
                                "stored in storage domain (name '{}', id '{}')",
                        vmForLogging.getName(), vmForLogging.getId(), storageDomainForMemory.getName(),
                        storageDomainForMemory.getId());
            }
        }
        return storageDomainForMemory;
    }

    public void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> memoryDisks) {
        for (DiskImage disk : memoryDisks) {
            disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomain.getId())));
        }
        /*
        There should be two disks in the disksList, first of which is memory disk.
        Only its volume type should be modified.
         */
        updateDiskVolumeType(storageDomain.getStorageType(), memoryDisks.get(0));
    }

    protected StorageDomain findStorageDomainForMemory(List<StorageDomain> domainsInPool, List<DiskImage> memoryDisks,
            Collection<DiskImage> vmDisks) {
        domainsInPool = filterStorageDomains(domainsInPool, memoryDisks);
        sortStorageDomains(domainsInPool, vmDisks);
        return domainsInPool.isEmpty() ? null : domainsInPool.get(0);
    }

    protected List<? extends StorageDomainFilter> getStorageDomainFilters() {
        return Arrays.asList(new StorageDomainStatusFilter(),
                new StorageDomainTypeFilter(),
                new StorageDomainSpaceRequirementsFilter());
    }

    protected List<? extends Comparator<StorageDomain>> getStorageDomainComparators(
            List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        return Arrays.asList(new StorageDomainNumberOfVmDisksComparator(domainsInPool, vmDisks),
                new StorageTypeComparator(),
                new StorageDomainAvailableDiskSizeComparator());
    }

    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, List<DiskImage> memoryDisks) {
        for (StorageDomainFilter storageDomainFilter : getStorageDomainFilters()) {
            domainsInPool = storageDomainFilter.filterStorageDomains(domainsInPool, memoryDisks);
        }
        return domainsInPool;
    }

    protected void sortStorageDomains(List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        Comparator<StorageDomain> comp = null;
        // When there is more than one comparator, a nested sort is performed.
        for (Comparator<StorageDomain> comparator : getStorageDomainComparators(domainsInPool, vmDisks)) {
            // A reversed sort will be performed to get the "biggest" storage domain first.
            comp = (comp == null) ? comparator.reversed() : comp.thenComparing(comparator.reversed());
        }
        Collections.sort(domainsInPool, comp);
    }

    private void updateDiskVolumeType(StorageType storageType, DiskImage disk) {
        VolumeType volumeType = storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        disk.setVolumeType(volumeType);
    }
}
