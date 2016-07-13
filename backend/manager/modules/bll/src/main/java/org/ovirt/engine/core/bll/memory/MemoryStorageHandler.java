package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.memory.sdcomparators.StorageDomainNumberOfVmDisksComparator;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainSpaceRequirementsFilter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
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
        return domainsInPool.stream().findFirst().orElse(null);
    }

    protected List<Predicate<StorageDomain>> getStorageDomainFilters(List<DiskImage> memoryDisks) {
        return Arrays.asList(ACTIVE_DOMAINS_PREDICATE,
                DATA_DOMAINS_PREDICATE,
                new StorageDomainSpaceRequirementsFilter(memoryDisks));
    }

    protected List<Comparator<StorageDomain>> getStorageDomainComparators(Collection<DiskImage> vmDisks) {
        return Arrays.asList(new StorageDomainNumberOfVmDisksComparator(vmDisks),
                SHARED_FIRST_COMPARATOR,
                FILE_FIRST_COMPARATOR,
                AVAILABLE_SIZE_COMPARATOR);
    }

    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, List<DiskImage> memoryDisks) {
        Predicate<StorageDomain> predicate =
                getStorageDomainFilters(memoryDisks).stream().reduce(Predicate::and).orElse(t -> true);
        return domainsInPool.stream().filter(predicate).collect(Collectors.toList());
    }

    protected void sortStorageDomains(List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        Comparator<StorageDomain> comp =
                getStorageDomainComparators(vmDisks).stream().reduce(Comparator::thenComparing).orElse(null);

        Collections.sort(domainsInPool, comp);
    }

    private void updateDiskVolumeType(StorageType storageType, DiskImage disk) {
        VolumeType volumeType = storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        disk.setVolumeType(volumeType);
    }

    /* Predicates */

    public static final Predicate<StorageDomain> ACTIVE_DOMAINS_PREDICATE =
            d -> d.getStatus() == StorageDomainStatus.Active;

    public static final Predicate<StorageDomain> DATA_DOMAINS_PREDICATE =
            d -> d.getStorageDomainType().isDataDomain();

    /* Comparators */

    public static final Comparator<StorageDomain> SHARED_FIRST_COMPARATOR =
            Comparator.comparing(StorageDomain::isShared).reversed();

    public static final Comparator<StorageDomain> FILE_FIRST_COMPARATOR =
            Comparator.<StorageDomain, Boolean>comparing(s -> s.getStorageType().isFileDomain()).reversed();

    public static final Comparator<StorageDomain> AVAILABLE_SIZE_COMPARATOR =
            Comparator.comparing(StorageDomain::getAvailableDiskSize).reversed();

}
