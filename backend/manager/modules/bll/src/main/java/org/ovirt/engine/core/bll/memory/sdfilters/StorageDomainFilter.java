package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;


public abstract class StorageDomainFilter {

    public List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, List<DiskImage> memoryDisks) {
        return domainsInPool.stream().filter(getPredicate(memoryDisks)).collect(Collectors.toList());
    }

    protected abstract Predicate<StorageDomain> getPredicate(final List<DiskImage> memoryDisks);
}
