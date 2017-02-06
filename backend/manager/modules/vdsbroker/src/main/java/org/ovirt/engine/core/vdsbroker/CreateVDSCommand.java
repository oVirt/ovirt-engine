package org.ovirt.engine.core.vdsbroker;

import java.util.Date;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

public class CreateVDSCommand<P extends CreateVDSCommandParameters> extends ManagingVmCommand<P> {

    @Inject
    private VmDao vmDao;

    public CreateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        final VM vm = getParameters().getVm();

        if (!resourceManager.addAsyncRunningVm(vm.getId())) {
            log.info("Vm Running failed - vm '{}'({}) already running", vm.getName(), vm.getId());
            getVDSReturnValue().setReturnValue(vm.getStatus());
            return;
        }

        vm.setLastStartTime(new Date());
        VDSReturnValue vdsReturnValue = null;
        try {
            vdsReturnValue = runCreateVDSCommand();
            if (vdsReturnValue.getSucceeded()) {
                if (!getParameters().isRunInUnknownStatus()) {
                    vmDao.saveIsInitialized(vm.getId(), true);
                    vm.setStopReason(null);
                    vm.setInitialized(true);
                    vm.setRunOnVds(getParameters().getVdsId());
                    vmManager.update(vm.getDynamicData());
                }
            } else {
                handleCommandResult(vdsReturnValue);
                resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            }
        } catch (Exception e) {
            log.error("Failed to create VM: {}", e.getMessage());
            log.error("Exception", e);
            if (vdsReturnValue != null && !vdsReturnValue.getSucceeded()) {
                resourceManager.removeAsyncRunningVm(getParameters().getVmId());
            }
            throw new RuntimeException(e);
        }

        getVDSReturnValue().setReturnValue(vm.getStatus());
    }

    private VDSReturnValue runCreateVDSCommand() {
        final VM vm = getParameters().getVm();
        if (isSysprepUsed(vm)) {
            // use answer file to run after sysprep.
            CreateVDSCommandParameters createVmFromSysPrepParam =
                    new CreateVDSCommandParameters(getParameters().getVdsId(), vm);
            createVmFromSysPrepParam.setSysPrepParams(getParameters().getSysPrepParams());
            return resourceManager.runVdsCommand(VDSCommandType.CreateVmFromSysPrep, createVmFromSysPrepParam);
        } else if (isCloudInitUsed(vm)) {
            return resourceManager.runVdsCommand(VDSCommandType.CreateVmFromCloudInit, getParameters());
        } else {
            // normal run.
            return resourceManager.runVdsCommand(VDSCommandType.CreateBroker, getParameters());
        }
    }

    private boolean isSysprepUsed(VM vm) {
        return vm.getInitializationType() == InitializationType.Sysprep
                && SimpleDependencyInjector.getInstance().get(OsRepository.class).isWindows(vm.getVmOsId())
                && (vm.getFloppyPath() == null || "".equals(vm.getFloppyPath()));
    }

    private boolean isCloudInitUsed(VM vm) {
        return vm.getInitializationType() == InitializationType.CloudInit
                && !SimpleDependencyInjector.getInstance().get(OsRepository.class).isWindows(vm.getVmOsId());
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
