package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;

/**
 * Algorithm. For server with desktops and server without desktop server with
 * desktops will be chosen. For 2 servers with desktops server with less
 * desktops will be chosen.
 */
public class PowerSaveComparer extends EvenlyDistributeComparer {

    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {

        boolean returnValue = false;

        boolean x_has_zero = (x.getVmCount() == 0);
        boolean y_has_zero = (y.getVmCount() == 0);

        if (x_has_zero) {
            if (!y_has_zero) {
                returnValue = true;
            }
        } else if (!y_has_zero) {
            return super.IsBetter(x, y, vm);
        }
        return returnValue;
    }
}
