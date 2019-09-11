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
    private static final long POWER_OFF_TIMEOUT_NANOSEC = 20000000000L; // 20 sec, in nanoseconds

    @Inject
    private VmDynamicDao vmDynamicDao;

    public DestroyVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        if (getParameters().getGracefully()) {
            super.executeVDSCommand();
            return;
        }

        long prevTimeout = resourceManager.getVmManager(getParameters().getVmId())
                .setPowerOffTimeout(System.nanoTime() + POWER_OFF_TIMEOUT_NANOSEC);

        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.Destroy,
                getParameters());

        if (!vdsReturnValue.getSucceeded()) {
            resourceManager.getVmManager(getParameters().getVmId()).setPowerOffTimeout(prevTimeout);
            if (vdsReturnValue.getExceptionObject() != null) {
                logFailureToDestroy(vdsReturnValue);
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
                getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
                getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
            }
        } else {
            VmDynamic vm = vmDynamicDao.get(getParameters().getVmId());
            vm.setStopReason(getParameters().getReason());
            resourceManager.getVmManager(getParameters().getVmId()).update(vm);
        }
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                VDSCommandType.Destroy,
                getParameters());

        if (vdsReturnValue.getSucceeded()) {
            resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            VmDynamic vm = vmDynamicDao.get(getParameters().getVmId());
            changeStatus(vm);
            vm.setStopReason(getParameters().getReason());
            vmManager.update(vm);
            getVDSReturnValue().setReturnValue(vm.getStatus());
        } else if (vdsReturnValue.getExceptionObject() != null) {
            logFailureToDestroy(vdsReturnValue);
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
            getVDSReturnValue().setExceptionObject(vdsReturnValue.getExceptionObject());
            getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
        }
    }

    private void logFailureToDestroy(VDSReturnValue vdsReturnValue) {
        log.error("Failed to destroy VM '{}' in VDS = '{}' , error = '{}'",
                getParameters().getVmId(),
                getParameters().getVdsId(),
                vdsReturnValue.getExceptionString());
    }

    private void changeStatus(VmDynamic curVm) {
        // do the state transition only if that VM is really running on SRC
        if (getParameters().getVdsId().equals(curVm.getRunOnVds())) {
            resourceManager.internalSetVmStatus(curVm, VMStatus.PoweringDown);
        }
    }
}
