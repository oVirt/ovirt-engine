package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
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
        return findStorageDomainForMemory(domainsInPool, disksList);
    }

    protected StorageDomain findStorageDomainForMemory(List<StorageDomain> domainsInPool, List<DiskImage> disksList) {
        for (StorageDomain currDomain : domainsInPool) {

            updateDisksStorage(currDomain, disksList);
            if (currDomain.getStorageDomainType().isDataDomain()
                    && currDomain.getStatus() == StorageDomainStatus.Active
                    && validateSpaceRequirements(currDomain, disksList)) {
                return currDomain;
            }
        }
        return null;
    }

    private void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        for (DiskImage disk : disksList) {
            disk.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomain.getId())));
        }
        /*
        There should be two disks in the disksList, first of which is memory disk.
        Only its volume type should be modified.
         */
        updateDiskVolumeType(storageDomain.getStorageType(), disksList.get(0));
    }

    private void updateDiskVolumeType(StorageType storageType, DiskImage disk) {
        VolumeType volumeType = storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
        disk.setVolumeType(volumeType);
    }

    private boolean validateSpaceRequirements(StorageDomain storageDomain, List<DiskImage> disksList) {
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);
        return (storageDomainValidator.isDomainWithinThresholds().isValid() &&
                storageDomainValidator.hasSpaceForClonedDisks(disksList).isValid());
    }
}
