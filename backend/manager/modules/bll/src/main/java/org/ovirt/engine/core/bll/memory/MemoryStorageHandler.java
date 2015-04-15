package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainSpaceRequirementsFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainStatusFilter;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainTypeFilter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MemoryStorageHandler {

    private static final MemoryStorageHandler instance = new MemoryStorageHandler();

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
     * @param disksList
     *           Disks for which space is needed
     * @return storage domain in the given pool with at least the required amount of free space,
     *         or null if no such storage domain exists in the pool
     */
    public StorageDomain findStorageDomainForMemory(Guid storagePoolId, List<DiskImage> disksList) {
        List<StorageDomain> domainsInPool =
                DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(storagePoolId);
        StorageDomain storageDomainForMemory = findStorageDomainForMemory(domainsInPool, disksList);
        if (storageDomainForMemory != null) {
            updateDisksStorage(storageDomainForMemory, disksList);
        }
        return storageDomainForMemory;
    }

    public void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        for (DiskImage disk : disksList) {
            disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomain.getId())));
        }
        /*
        There should be two disks in the disksList, first of which is memory disk.
        Only its volume type should be modified.
         */
        updateDiskVolumeType(storageDomain.getStorageType(), disksList.get(0));
    }

    protected StorageDomain findStorageDomainForMemory(List<StorageDomain> domainsInPool, List<DiskImage> disksList) {
        domainsInPool = filterStorageDomains(domainsInPool, disksList);
        return domainsInPool.isEmpty() ? null : domainsInPool.get(0);
    }

    protected List<? extends StorageDomainFilter> getStorageDomainFilters() {
        return Arrays.asList(new StorageDomainStatusFilter(),
                new StorageDomainTypeFilter(),
                new StorageDomainSpaceRequirementsFilter());
    }

    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, List<DiskImage> disksList) {
        for (StorageDomainFilter storageDomainFilter : getStorageDomainFilters()) {
            domainsInPool = storageDomainFilter.filterStorageDomains(domainsInPool, disksList);
        }
        return domainsInPool;
    }

    private void updateDiskVolumeType(StorageType storageType, DiskImage disk) {
        VolumeType volumeType = storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        disk.setVolumeType(volumeType);
    }
}
