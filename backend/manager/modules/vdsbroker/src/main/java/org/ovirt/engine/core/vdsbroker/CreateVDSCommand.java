package org.ovirt.engine.core.vdsbroker;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

public class CreateVDSCommand<P extends CreateVDSCommandParameters> extends ManagingVmCommand<P> {

    public CreateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VM vm = getParameters().getVm();
        getVDSReturnValue().setReturnValue(vm.getStatus());
        if (!resourceManager.addAsyncRunningVm(vm.getId())) {
            log.info("Vm Running failed - vm '{}'({}) already running", vm.getName(), vm.getId());
            return;
        }

        Date now = new Date();
        VDSReturnValue vdsReturnValue = null;
        try {
            vdsReturnValue = resourceManager.runVdsCommand(VDSCommandType.CreateBroker, getParameters());
            if (!vdsReturnValue.getSucceeded()) {
                handleCommandResult(vdsReturnValue);
                resourceManager.removeAsyncRunningVm(getParameters().getVmId());
                return;
            }

            if (!getParameters().isRunInUnknownStatus()) {
                boolean vmIsBooting = StringUtils.isEmpty(getParameters().getHibernationVolHandle())
                        && getParameters().getMemoryConfImage() == null;
                if (vmIsBooting) {
                    vm.setBootTime(now);
                    vm.setDowntime(0);
                    vm.setRuntimeName(vm.getName());
                } else {
                    vm.setDowntime(vm.getDowntime() + now.getTime() - getParameters().getDownSince().getTime());
                }
                vm.setLastStartTime(now);
                vm.setStopReason(null);
                vm.setRunOnVds(getParameters().getVdsId());
                vmManager.update(vm.getDynamicData());
            }
        } catch (Exception e) {
            log.error("Failed to create VM", e);
            //log.debug("Exception", e);
            if (vdsReturnValue != null && !vdsReturnValue.getSucceeded()) {
                resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            }
            throw new RuntimeException(e);
        }

        getVDSReturnValue().setReturnValue(vm.getStatus());
    }

    private void handleCommandResult(VDSReturnValue vdsReturnValue) {
        if (!vdsReturnValue.getSucceeded() && vdsReturnValue.getExceptionObject() != null) {
            if (vdsReturnValue.getExceptionObject() instanceof VDSGenericException) {
                log.error("VDS::create Failed creating vm '{}' in vds = '{}' error = '{}'",
                        getParameters().getVm().getName(), getParameters().getVdsId(),
                        vdsReturnValue.getExceptionString());
                getVDSReturnValue().setReturnValue(VMStatus.Down);
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setExceptionString(vdsReturnValue.getExceptionString());
                getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
            } else {
                throw vdsReturnValue.getExceptionObject();
            }
        }
    }

}
