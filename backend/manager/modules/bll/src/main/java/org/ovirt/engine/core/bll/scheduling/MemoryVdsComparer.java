package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;

/**
 * This comparer chose Vds with more memory available as best
 */
public class MemoryVdsComparer extends VdsComparer {
    @Override
    public boolean isBetter(VDS x, VDS y, VM vm) {
        return ((x.getPhysicalMemMb() - x.getMemCommited()) < (y.getPhysicalMemMb() - y.getMemCommited()));

    }

    @Override
    public void bestVdsProcedure(VDS x) {

    }
}
