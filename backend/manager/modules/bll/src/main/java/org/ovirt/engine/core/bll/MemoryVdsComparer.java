package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * This comparer chose Vds with more memory available as best
 */
public class MemoryVdsComparer extends VdsComparer {
    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        // C# TO JAVA CONVERTER TODO TASK: Arithmetic operations involving
        // nullable type instances are not converted to null-value logic:
        return ((x.getphysical_mem_mb() - x.getmem_commited()) < (y.getphysical_mem_mb() - y.getmem_commited()));

    }

    @Override
    public void BestVdsProcedure(VDS x) {

    }
}
