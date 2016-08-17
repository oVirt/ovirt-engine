package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;

public class HostSpmPriorityComparator implements Comparator<VDS>, Serializable {
    private static final long serialVersionUID = -7447013630170911071L;

    @Override
    public int compare(VDS host1, VDS host2) {
        return Integer.compare(host2.getVdsSpmPriority(), host1.getVdsSpmPriority());
    }
}
