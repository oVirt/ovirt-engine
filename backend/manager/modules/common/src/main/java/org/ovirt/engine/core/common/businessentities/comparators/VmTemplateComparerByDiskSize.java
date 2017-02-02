package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class VmTemplateComparerByDiskSize implements Comparator<VmTemplate>, Serializable {
    private static final long serialVersionUID = -1620249078971769528L;

    private Map<VmTemplate, Double> actualDiskSizeCache = new WeakHashMap<>();

    private double getCachedActualDiskSize(VmTemplate template) {
        final Double cacheSize = actualDiskSizeCache.get(template);
        if (cacheSize != null) {
            return cacheSize;
        }
        final double computedSize = template.getActualDiskSize();
        actualDiskSizeCache.put(template, computedSize);
        return computedSize;
    }

    @Override
    public int compare(VmTemplate x, VmTemplate y) {
        return (int) (getCachedActualDiskSize(x) - getCachedActualDiskSize(y));
    }
}
