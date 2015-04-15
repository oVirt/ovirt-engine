package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.utils.linq.Predicate;

public class StorageDomainStatusFilter extends StorageDomainFilter {

    @Override
    protected Predicate<StorageDomain> getPredicate(final List<DiskImage> disksList) {
        return new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain storageDomain) {
                return storageDomain.getStatus() == StorageDomainStatus.Active;
            }
        };
    }
}
