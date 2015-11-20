package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsFreeMemoryChecker {

    private RunVmDelayer delayer;

    private static final Logger log = LoggerFactory.getLogger(VdsFreeMemoryChecker.class);

    public VdsFreeMemoryChecker(RunVmDelayer delayer) {
        this.delayer = delayer;
    }

    public boolean evaluate(VDS vds, VM vm) {
        // first check if this host has enough free memory to run the VM.
        if (!SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm)) {

            if (vds.getPendingVmemSize() == 0) {
                // there are no pending VMs to run - we hit the hard limit of memory, no special treatment
                return false;
            }

            log.debug("not enough memory on host. throttling...");

            // not enough memory to run the vm. delay execution to free up pending memory.
            delayer.delay(vds.getId());

            // fetch a fresh vds and check its memory again
            vds = DbFacade.getInstance().getVdsDao().get(vds.getId());

            // check free memory on the updated host
            return SlaValidator.getInstance().hasOvercommitMemoryToRunVM(vds, vm);
        }
        return true;
    }
}
