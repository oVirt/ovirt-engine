package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;
import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class StorageDomainTypeFilter extends StorageDomainFilter {

    @Override
    protected Predicate<StorageDomain> getPredicate(List<DiskImage> memoryDisks) {
        return storageDomain -> storageDomain.getStorageDomainType().isDataDomain();
    }
}
