package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;

/**
 * Base class for comparing between Hosts
 */
public abstract class VdsComparer {
    /**
     * Factory method, creates necessary comparer
     *
     * @return
     */
    public static VdsComparer CreateComparer(VdsSelectionAlgorithm selectionAlgorithm) {
        switch (selectionAlgorithm) {
        case EvenlyDistribute:
            return new EvenlyDistributeComparer();
        case PowerSave:
            return new PowerSaveComparer();
        default:
            return new NoneComparer();
        }
    }

    /**
     * Base abstract function for finish best Vds treatment
     *
     * @param x
     */
    public abstract void bestVdsProcedure(VDS x);

    /**
     * Base abstract function to compare between two VDSs
     *
     * @param x
     * @param y
     * @param vm
     * @return
     */
    public abstract boolean isBetter(VDS x, VDS y, VM vm);
}
