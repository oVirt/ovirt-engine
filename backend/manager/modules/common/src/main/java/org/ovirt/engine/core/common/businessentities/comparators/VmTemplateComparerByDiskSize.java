package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class VmTemplateComparerByDiskSize implements Comparator<VmTemplate>, Serializable {
    private static final long serialVersionUID = -1620249078971769528L;

    @Override
    public int compare(VmTemplate x, VmTemplate y) {
        return (int) (x.getActualDiskSize() - y.getActualDiskSize());
    }
}
