package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.dao.VmDynamicDao;

public class ResetVmVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends ManagingVmCommand<VdsAndVmIDVDSParametersBase> {

    @Inject
    private VmDynamicDao vmDynamicDao;

    public ResetVmVDSCommand(VdsAndVmIDVDSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.ResetVmBroker,
                getParameters());

        if (vdsReturnValue.getSucceeded()) {
            vmDynamicDao.updateStatus(getParameters().getVmId(), VMStatus.RebootInProgress);
        } else if (vdsReturnValue.getExceptionObject() != null) {
            log.error("Failed to reset VM '{}' in VDS = '{}' error = '{}'",
                    getParameters().getVmId(),
                    getParameters().getVdsId(),
                    vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
    }

}
