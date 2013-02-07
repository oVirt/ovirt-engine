package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * This comparer chose Vds with less vms running regarding if vds will be
 * overcommited after its will run vm.
 */
public class EvenlyDistributeVDComparer extends VdsComparer {
    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        if (x.getVmsCoresCount() == null || y.getVmsCoresCount() == null) {
            return false;
        }
        boolean returnValue;
        returnValue = ((double) x.getVmsCoresCount() / x.getCpuCores()) > ((double) y.getVmsCoresCount() / y
                .getCpuCores());
        return returnValue;
    }

    @Override
    public void BestVdsProcedure(VDS x) {
        x.setVmCount(x.getVmCount() + 1);
    }
}
