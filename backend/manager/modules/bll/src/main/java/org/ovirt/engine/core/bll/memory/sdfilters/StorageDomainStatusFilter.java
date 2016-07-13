package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;


public class StorageDomainStatusFilter implements Predicate<StorageDomain> {
    @Override
    public boolean test(StorageDomain storageDomain) {
        return storageDomain.getStatus() == StorageDomainStatus.Active;
    }
}
