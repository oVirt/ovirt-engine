package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.memory.sdcomparators.StorageDomainNumberOfVmDisksComparator;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainSpaceRequirementsFilter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MemoryStorageHandler {

    private static final Logger log = LoggerFactory.getLogger(MemoryStorageHandler.class);

    @Inject
    private StorageDomainDao storageDomainDao;

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
    public StorageDomain findStorageDomainForMemory(Guid storagePoolId, MemoryDisks memoryDisks,
            Collection<DiskImage> vmDisks, VM vmForLogging) {
        List<StorageDomain> domainsInPool = storageDomainDao.getAllForStoragePool(storagePoolId);
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

    public void updateDisksStorage(StorageDomain storageDomain, MemoryDisks memoryDisks) {
        memoryDisks.getMemoryDisk().setStorageIds(new ArrayList<>(List.of(storageDomain.getId())));
        memoryDisks.getMetadataDisk().setStorageIds(new ArrayList<>(List.of(storageDomain.getId())));

        VolumeType volumeType = storageDomain.getStorageType().isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        memoryDisks.getMemoryDisk().setVolumeType(volumeType);
    }

    protected StorageDomain findStorageDomainForMemory(List<StorageDomain> domainsInPool, MemoryDisks memoryDisks,
            Collection<DiskImage> vmDisks) {
        domainsInPool = filterStorageDomains(domainsInPool, memoryDisks);
        domainsInPool = sortStorageDomains(domainsInPool, vmDisks);
        return domainsInPool.stream().findFirst().orElse(null);
    }

    protected List<Predicate<StorageDomain>> getStorageDomainFilters(MemoryDisks memoryDisks) {
        return Arrays.asList(ACTIVE_DOMAINS_PREDICATE,
                DATA_DOMAINS_PREDICATE,
                new StorageDomainSpaceRequirementsFilter(this, memoryDisks));
    }

    protected List<Comparator<StorageDomain>> getStorageDomainComparators(Collection<DiskImage> vmDisks) {
        return Arrays.asList(new StorageDomainNumberOfVmDisksComparator(vmDisks),
                SHARED_FIRST_COMPARATOR,
                FILE_FIRST_COMPARATOR,
                AVAILABLE_SIZE_COMPARATOR);
    }

    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, MemoryDisks memoryDisks) {
        Predicate<StorageDomain> predicate =
                getStorageDomainFilters(memoryDisks).stream().reduce(Predicate::and).orElse(t -> true);
        return domainsInPool.stream().filter(predicate).collect(Collectors.toList());
    }

    protected List<StorageDomain> sortStorageDomains(List<StorageDomain> domainsInPool, Collection<DiskImage> vmDisks) {
        Comparator<StorageDomain> comp =
                getStorageDomainComparators(vmDisks).stream().reduce(Comparator::thenComparing).orElse(null);
        domainsInPool = domainsInPool.stream().filter(s -> s.getAvailableDiskSize() != null).collect(Collectors.toList());
        domainsInPool.sort(comp);
        return domainsInPool;
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
