package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;
import java.util.function.Predicate;

import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;


public class StorageDomainSpaceRequirementsFilter extends StorageDomainFilter {

    @Override
    protected Predicate<StorageDomain> getPredicate(final List<DiskImage> memoryDisks) {
        return storageDomain -> {
            updateDisksStorage(storageDomain, memoryDisks);
            StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
            return storageDomainValidator.isDomainWithinThresholds().isValid() &&
                    storageDomainValidator.hasSpaceForClonedDisks(memoryDisks).isValid();
        };
    }

    protected void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        MemoryStorageHandler.getInstance().updateDisksStorage(storageDomain, disksList);
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }
}
