package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.StorageDomain;

public class StorageDomainTypeFilter implements Predicate<StorageDomain> {
    @Override
    public boolean test(StorageDomain storageDomain) {
        return storageDomain.getStorageDomainType().isDataDomain();
    }
}
