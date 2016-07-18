package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVmStatusVDSCommand<P extends SetVmStatusVDSCommandParameters> extends VDSCommandBase<P> {

    private static final Logger log = LoggerFactory.getLogger(SetVmStatusVDSCommand.class);

    @Inject
    private VmDynamicDao vmDynamicDao;

    public SetVmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        final VMStatus status = getParameters().getStatus();
        if (status == null) {
            log.warn("got request to change the status of VM '{}' to null,  ignoring", getParameters().getVmId());
            return;
        }

        VmDynamic vmDynamic = vmDynamicDao.get(getParameters().getVmId());
        vmDynamic.setStatus(status);
        if (status.isNotRunning()) {
            resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            resourceManager.internalSetVmStatus(vmDynamic, status, getParameters().getExitStatus());
            resourceManager.getVmManager(getParameters().getVmId()).update(new VmStatistics(getParameters().getVmId()));
            // TODO: update network statistics
        } else if (status == VMStatus.Unknown) {
            resourceManager.removeAsyncRunningVm(getParameters().getVmId());
        }
        vmDynamicDao.update(vmDynamic);
    }
}
