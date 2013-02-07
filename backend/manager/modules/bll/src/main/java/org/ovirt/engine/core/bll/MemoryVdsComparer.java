package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * This comparer chose Vds with more memory available as best
 */
public class MemoryVdsComparer extends VdsComparer {
    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        return ((x.getPhysicalMemMb() - x.getMemCommited()) < (y.getPhysicalMemMb() - y.getMemCommited()));

    }

    @Override
    public void BestVdsProcedure(VDS x) {

    }
}
