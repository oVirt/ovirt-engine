package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.dao.VmDynamicDao;

public class SetVmStatusVDSCommand<P extends SetVmStatusVDSCommandParameters> extends VDSCommandBase<P> {

    @Inject
    private VmDynamicDao vmDynamicDao;

    public SetVmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        VmDynamic vmDynamic = vmDynamicDao.get(getParameters().getVmId());
        resourceManager.removeAsyncRunningVm(getParameters().getVmId());
        resourceManager.internalSetVmStatus(vmDynamic, getParameters().getStatus(), getParameters().getExitStatus());
        resourceManager.getVmManager(getParameters().getVmId()).update(new VmStatistics(getParameters().getVmId()));
        // TODO: update network statistics
        resourceManager.getVmManager(getParameters().getVmId()).update(vmDynamic);
    }
}
