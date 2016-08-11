package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestroyVmVDSCommand<P extends DestroyVmVDSCommandParameters> extends ManagingVmCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(DestroyVmVDSCommand.class);

    @Inject
    private VmDynamicDao vmDynamicDao;

    public DestroyVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        resourceManager.removeAsyncRunningVm(getParameters().getVmId());

        VmDynamic curVm = vmDynamicDao.get(getParameters().getVmId());

        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.Destroy,
                getParameters());

        if (vdsReturnValue.getSucceeded()) {
            if (curVm.getStatus() == VMStatus.Down) {
                getVDSReturnValue().setReturnValue(VMStatus.Down);
            }

            changeStatus(getParameters(), curVm);
            curVm.setStopReason(getParameters().getReason());
            vmManager.update(curVm);
            getVDSReturnValue().setReturnValue(curVm.getStatus());
        } else if (vdsReturnValue.getExceptionObject() != null) {
            log.error("Failed to destroy VM '{}' in VDS = '{}' , error = '{}'",
                    getParameters().getVmId(),
                    getParameters().getVdsId(),
                    vdsReturnValue.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
    }

    private void changeStatus(DestroyVmVDSCommandParameters parameters, VmDynamic curVm) {
        // do the state transition only if that VM is really running on SRC
        if (getParameters().getVdsId().equals(curVm.getRunOnVds())) {
            resourceManager.internalSetVmStatus(curVm, VMStatus.PoweringDown);
        }
    }
}
