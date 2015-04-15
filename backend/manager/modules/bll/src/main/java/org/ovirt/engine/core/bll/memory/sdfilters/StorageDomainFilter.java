package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.utils.linq.Predicate;

public abstract class StorageDomainFilter {

    public List<StorageDomain> filterStorageDomains(List<StorageDomain> domainsInPool, List<DiskImage> disksList) {
        domainsInPool = new LinkedList<>(domainsInPool);
        Iterator<StorageDomain> iterator = domainsInPool.iterator();
        while (iterator.hasNext()) {
            if (!getPredicate(disksList).eval(iterator.next())) {
                iterator.remove();
            }
        }
        return domainsInPool;
    }

    protected abstract Predicate<StorageDomain> getPredicate(final List<DiskImage> disksList);
}
