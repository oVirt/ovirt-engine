package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;
import java.util.function.Predicate;

import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;


public class StorageDomainSpaceRequirementsFilter implements Predicate<StorageDomain> {

    private List<DiskImage> memoryDisks;

    public StorageDomainSpaceRequirementsFilter(List<DiskImage> memoryDisks) {
        this.memoryDisks = memoryDisks;
    }

    @Override
    public boolean test(StorageDomain storageDomain) {
        updateDisksStorage(storageDomain, memoryDisks);
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
        return storageDomainValidator.isDomainWithinThresholds().isValid() &&
                storageDomainValidator.hasSpaceForClonedDisks(memoryDisks).isValid();
    }

    protected void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        MemoryStorageHandler.getInstance().updateDisksStorage(storageDomain, disksList);
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }
}
