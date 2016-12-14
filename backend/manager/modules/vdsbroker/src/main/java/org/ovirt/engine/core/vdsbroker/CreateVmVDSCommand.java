package org.ovirt.engine.core.vdsbroker;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

public class CreateVmVDSCommand<P extends CreateVmVDSCommandParameters> extends ManagingVmCommand<P> {

    @Inject
    private VmDao vmDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private SnapshotDao snapshotDao;

    public CreateVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        final VM vm = getParameters().getVm();
        vm.setLastStartTime(new Date());
        if (canExecute() && resourceManager.addAsyncRunningVm(vm.getId())) {
            VDSReturnValue vdsReturnValue = null;
            try {
                vdsReturnValue = runCreateVDSCommand();
                if (vdsReturnValue.getSucceeded()) {
                    vmDao.saveIsInitialized(vm.getId(), true);
                    vm.setStopReason(null);
                    vm.setInitialized(true);
                    vm.setRunOnVds(getParameters().getVdsId());
                    vmManager.update(vm.getDynamicData());
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
        }
        getVDSReturnValue().setReturnValue(vm.getStatus());
    }

    private VDSReturnValue runCreateVDSCommand() {
        final VM vm = getParameters().getVm();
        if (vm.isSysprepUsed()) {
            // use answer file to run after sysprep.
            CreateVmVDSCommandParameters createVmFromSysPrepParam =
                    new CreateVmVDSCommandParameters(getParameters().getVdsId(), vm);
            createVmFromSysPrepParam.setSysPrepParams(getParameters().getSysPrepParams());
            return resourceManager.runVdsCommand(VDSCommandType.CreateVmFromSysPrep, createVmFromSysPrepParam);
        } else if (vm.isCloudInitUsed()) {
            return resourceManager.runVdsCommand(VDSCommandType.CreateVmFromCloudInit, getParameters());
        } else {
            // normal run.
            return resourceManager.runVdsCommand(VDSCommandType.Create, getParameters());
        }
    }

    private boolean canExecute() {
        Guid guid = getParameters().getVm().getId();
        String vmName = getParameters().getVm().getName();
        VmDynamic vmDynamicFromDb = vmDynamicDao.get(guid);

        if (resourceManager.isVmDuringInitiating(getParameters().getVm().getId())) {
            log.info("Vm Running failed - vm '{}'({}) already running", vmName, guid);
            getVDSReturnValue().setReturnValue(vmDynamicFromDb.getStatus());
            return false;
        }

        VMStatus vmStatus = vmDynamicFromDb.getStatus();
        if (vmStatus == VMStatus.ImageLocked) {
            log.info("VM Running failed - vm '{}'({}) - cannot run vm when image is locked", vmName, guid);
            return false;
        }

        List<Snapshot> snapshots = snapshotDao.getAll(guid);
        if (!snapshots.isEmpty() && SnapshotStatus.LOCKED == snapshots.get(snapshots.size() - 1).getStatus()) {
            log.info("VM Running failed - VM '{}'({}) - cannot run VM when VM during Snapshot", vmName, guid);
            return false;
        }

        return true;
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
