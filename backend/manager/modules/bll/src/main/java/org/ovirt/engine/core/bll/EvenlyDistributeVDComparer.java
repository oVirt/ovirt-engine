package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * This comparer chose Vds with less vms running regarding if vds will be
 * overcommited after its will run vm.
 */
public class EvenlyDistributeVDComparer extends VdsComparer {
    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        if (x.getvms_cores_count() == null || y.getvms_cores_count() == null) {
            return false;
        }
        boolean returnValue;
        returnValue = ((double) x.getvms_cores_count() / x.getcpu_cores()) > ((double) y.getvms_cores_count() / y
                .getcpu_cores());
        return returnValue;
    }

    @Override
    public void BestVdsProcedure(VDS x) {
        x.setvm_count(x.getvm_count() + 1);
    }
}
