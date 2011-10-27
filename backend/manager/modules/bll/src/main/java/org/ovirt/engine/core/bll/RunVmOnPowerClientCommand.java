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
            return getDestinationVds().getvds_id();
        } else {
            return null;
        }
        // return PowerClient != null ? PowerClient.vds_id : null;
    }

    @Override
    protected VMStatus CreateVm() {
        // Keep old memory to reset after the create call, so will be correct in
        // case of re-runs.
        int oldMemory = getVm().getStaticData().getmem_size_mb();
        // this chould be from a power client, but where a rerun occured and
        // current vds is not a power client
        if (getDestinationVds() != null && getDestinationVds().getvds_id().equals(getVdsId())) {
            AutoMemoryAdjust(getDestinationVds(), getVm());
        }
        getVm().getDynamicData().setguest_requested_memory(getVm().getStaticData().getmem_size_mb());

        VMStatus status = super.CreateVm();
        getVm().getStaticData().setmem_size_mb(oldMemory);
        return status;
    }
}
