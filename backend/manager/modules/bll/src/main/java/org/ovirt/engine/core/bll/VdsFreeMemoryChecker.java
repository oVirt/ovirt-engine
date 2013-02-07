package org.ovirt.engine.core.bll;

import org.apache.commons.logging.Log;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsFreeMemoryChecker {

    private RunVmDelayer delayer;

    private static Log log = LogFactory.getLog(VdsFreeMemoryChecker.class);

    public VdsFreeMemoryChecker(RunVmDelayer delayer) {
        this.delayer = delayer;
    }

    public boolean evaluate(VDS vds, VM vm) {
        // first check if this host has enough memory run the VM.
        if (!RunVmCommandBase.hasMemoryToRunVM(vds, vm)) {

            if (vds.getPendingVmemSize() == 0) {
                // there are no pending VMs to run - we hit the hard limit of memory, no special treatment
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("not enough memory on host. throttling...");
            }
            // not enough memory to run the vm. delay execution to free up pending memory.
            delayer.delay(vds.getId());

            // fetch a fresh vds and check its memory again
            vds = DbFacade.getInstance().getVdsDao().get(vds.getId());

            // check free memory on the updated host
            return RunVmCommandBase.hasMemoryToRunVM(vds, vm);
        }
        return true;
    }
}
