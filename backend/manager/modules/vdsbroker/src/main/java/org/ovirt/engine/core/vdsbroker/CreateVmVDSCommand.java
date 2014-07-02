package org.ovirt.engine.core.vdsbroker;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVmFromCloudInitVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVmFromSysPrepVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

public class CreateVmVDSCommand<P extends CreateVmVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public CreateVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        if (_vdsManager == null) {
            getVDSReturnValue().setSucceeded(false);
            return;
        }

        final VM vm = getParameters().getVm();
        vm.setLastStartTime(new Date());
        // if the VM is not suspended, it means that if there is 'hibernation volume'
        // set, it is actually memory from snapshot, thus it should be cleared right
        // after the VM started
        final boolean clearHibernationVolume = vm.getStatus() != VMStatus.Suspended;
        if (canExecute() && ResourceManager.getInstance().AddAsyncRunningVm(vm.getId())) {
            CreateVDSCommand<?> command = null;
            try {
                command = initCreateVDSCommand(vm);
                command.execute();
                if (command.getVDSReturnValue().getSucceeded()) {
                    saveSetInitializedToDb(vm.getId());

                    vm.setStopReason(null);
                    vm.setInitialized(true);
                    vm.setRunOnVds(getVdsId());
                    if (clearHibernationVolume) {
                        vm.setHibernationVolHandle(StringUtils.EMPTY);
                    }
                    DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
                } else {
                    handleCommandResult(command);
                    ResourceManager.getInstance().RemoveAsyncRunningVm(getParameters().getVmId());
                }
            } catch (Exception e) {
                log.error("Error in excuting CreateVmVDSCommand", e);
                if (command != null && !command.getVDSReturnValue().getSucceeded()) {
                    ResourceManager.getInstance().RemoveAsyncRunningVm(getParameters().getVmId());
                }
                throw new RuntimeException(e);
            }
        }
        getVDSReturnValue().setReturnValue(vm.getStatus());
    }

    private CreateVDSCommand<?> initCreateVDSCommand(VM vm) throws Exception {
        if (vm.isSysprepUsed()) {

            // use answer file to run after sysprep.
            CreateVmVDSCommandParameters createVmFromSysPrepParam =
                    new CreateVmVDSCommandParameters(
                            getVdsId(),
                            vm);
            createVmFromSysPrepParam.setSysPrepParams(getParameters().getSysPrepParams());
            return new CreateVmFromSysPrepVDSCommand<CreateVmVDSCommandParameters>(createVmFromSysPrepParam);
        }
        else if (vm.isCloudInitUsed()) {
            return new CreateVmFromCloudInitVDSCommand<CreateVmVDSCommandParameters>(getParameters());
        }
        else {
            // normal run.
            return new CreateVDSCommand<CreateVmVDSCommandParameters>(getParameters());
        }
    }

    private boolean canExecute() {

        Guid guid = getParameters().getVm().getId();
        String vmName = getParameters().getVm().getName();
        VmDynamic vmDynamicFromDb = DbFacade.getInstance().getVmDynamicDao().get(guid);
        if (ResourceManager.getInstance().IsVmDuringInitiating(getParameters().getVm().getId())) {
            log.infoFormat("Vm Running failed - vm {0}:{1} already running", guid, vmName);
            getVDSReturnValue().setReturnValue(vmDynamicFromDb.getStatus());
            return false;
        } else {
            VMStatus vmStatus = vmDynamicFromDb.getStatus();

            if (vmStatus == VMStatus.ImageLocked) {
                log.infoFormat("Vm Running failed - vm {0}:{1} - cannot run vm when image is locked", guid, vmName);
                return false;
            }
            if (vmDynamicFromDb.getStatus() != VMStatus.Down && vmDynamicFromDb.getStatus() != VMStatus.Suspended) {
                log.infoFormat("Vm Running failed - vm {0}:{1} already running, status {2}", guid, vmName, vmStatus);
                getVDSReturnValue().setReturnValue(vmDynamicFromDb.getStatus());
                return false;
            }

            List<Snapshot> snapshots = DbFacade.getInstance().getSnapshotDao().getAll(guid);

            if (!snapshots.isEmpty() && SnapshotStatus.LOCKED == snapshots.get(snapshots.size() - 1).getStatus()) {
                log.infoFormat("VM Running failed - VM {0}:{1} - cannot run VM when VM during Snapshot", guid, vmName);
                return false;
            }
        }
        return true;
    }

    private void handleCommandResult(CreateVDSCommand<?> command) {
        if (!command.getVDSReturnValue().getSucceeded() && command.getVDSReturnValue().getExceptionObject() != null) {
            if (command.getVDSReturnValue().getExceptionObject() instanceof VDSGenericException) {
                log.errorFormat("VDS::create Failed creating vm '{0}' in vds = {1} : {2} error = {3}",
                        getParameters().getVm().getName(), getVds().getId(), getVds().getName(),
                        command.getVDSReturnValue().getExceptionString());
                getVDSReturnValue().setReturnValue(VMStatus.Down);
                getVDSReturnValue().setSucceeded(false);
                getVDSReturnValue().setExceptionString(command.getVDSReturnValue().getExceptionString());
                getVDSReturnValue().setVdsError(command.getVDSReturnValue().getVdsError());
            } else {
                throw command.getVDSReturnValue().getExceptionObject();
            }
        }
    }

    private void saveSetInitializedToDb(final Guid vmId) {
        DbFacade.getInstance().getVmDao().saveIsInitialized(vmId, true);
    }

    private static final Log log = LogFactory.getLog(CreateVmVDSCommand.class);
}
