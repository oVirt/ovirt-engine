package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;

public class RunVmOnPowerClientCommand<T extends RunVmParams> extends RunVmOnDedicatedVdsCommand<T> {
    public RunVmOnPowerClientCommand(T runVmParams) {
        super(runVmParams);
        getVdsSelector().setCheckDestinationFirst(true);
    }

    @Override
    public Guid getAutoStartVdsId() {
        if (getDestinationVds() != null) {
            return getDestinationVds().getId();
        } else {
            return null;
        }
        // return PowerClient != null ? PowerClient.vds_id : null;
    }

    @Override
    protected VMStatus createVm() {
        // Keep old memory to reset after the create call, so will be correct in
        // case of re-runs.
        int oldMemory = getVm().getStaticData().getMemSizeMb();
        // this chould be from a power client, but where a rerun occured and
        // current vds is not a power client
        if (getDestinationVds() != null && getDestinationVds().getId().equals(getVdsId())) {
            AutoMemoryAdjust(getDestinationVds(), getVm());
        }
        getVm().getDynamicData().setguest_requested_memory(getVm().getStaticData().getMemSizeMb());

        VMStatus status = super.createVm();
        getVm().getStaticData().setMemSizeMb(oldMemory);
        return status;
    }
}
