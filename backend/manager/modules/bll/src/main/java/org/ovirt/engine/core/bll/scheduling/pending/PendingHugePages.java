package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class PendingHugePages extends PendingResource {
    private int pageSize;
    private int count;

    public PendingHugePages(Guid host,
            VM vm,
            int pageSize, int count) {
        super(host, vm);
        this.pageSize = pageSize;
        this.count = count;
    }

    public PendingHugePages(VDS host,
            VM vm, int pageSize, int count) {
        super(host, vm);
        this.pageSize = pageSize;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PendingHugePages that = (PendingHugePages) o;
        return pageSize == that.pageSize &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getVm(), that.getVm());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVm(), getHost(), pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getCount() {
        return count;
    }

    /**
     * Count the map of pending hugepages:
     *   maps hugepage size -> count
     */
    public static Map<Integer, Integer> collectForHost(PendingResourceManager manager, Guid host) {
        Map<Integer, Integer> pageCount = new HashMap<>();

        for (PendingHugePages resource: manager.pendingHostResources(host, PendingHugePages.class)) {
            pageCount.compute(resource.getPageSize(),
                    (hpsize, count) -> (count == null ? 0 : count) + resource.getCount());
        }

        return Collections.unmodifiableMap(pageCount);
    }
}
