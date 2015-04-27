package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;

import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.utils.linq.Predicate;

public class StorageDomainSpaceRequirementsFilter extends StorageDomainFilter {

    @Override
    protected Predicate<StorageDomain> getPredicate(final List<DiskImage> memoryDisks) {
        return new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain storageDomain) {
                updateDisksStorage(storageDomain, memoryDisks);
                StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
                return storageDomainValidator.isDomainWithinThresholds().isValid() &&
                        storageDomainValidator.hasSpaceForClonedDisks(memoryDisks).isValid();
            }
        };
    }

    protected void updateDisksStorage(StorageDomain storageDomain, List<DiskImage> disksList) {
        MemoryStorageHandler.getInstance().updateDisksStorage(storageDomain, disksList);
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }
}
