package org.ovirt.engine.core.vdsbroker;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVmFromSysPrepVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CreateVmFromSysPrepVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

public class CreateVmVDSCommand<P extends CreateVmVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public CreateVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {

        CreateVDSCommand<?> command = null;
        try {
            if (_vdsManager != null) {
                final VM vm = getParameters().getVm();
                if (CanExecute()) {
                    boolean canExecute = ResourceManager.getInstance().AddAsyncRunningVm(
                                vm.getId());
                    if (canExecute) {
                        if (isSysprepUsed(vm)) {
                            // use answer file to run after sysprep.
                            CreateVmFromSysPrepVDSCommandParameters createVmFromSysPrepParam =
                                    new CreateVmFromSysPrepVDSCommandParameters(
                                            getVdsId(),
                                            vm,
                                            vm.getName(),
                                            vm.getVmDomain());
                            createVmFromSysPrepParam.setSysPrepParams(getParameters().getSysPrepParams());
                            command =
                                    new CreateVmFromSysPrepVDSCommand<CreateVmFromSysPrepVDSCommandParameters>(createVmFromSysPrepParam);
                            command.execute();
                            if (command.getVDSReturnValue().getSucceeded()) {
                                vm.setInitialized(true);
                                saveSetInitializedToDb(vm.getId());
                            } else {
                                HandleCommandResult(command);
                            }
                        } else {
                            // normal run.
                            command = new CreateVDSCommand<CreateVmVDSCommandParameters>(getParameters());
                            command.execute();
                            HandleCommandResult(command);
                            vm.setInitialized(true);
                            saveSetInitializedToDb(vm.getId());
                        }

                        if (command.getVDSReturnValue().getSucceeded()) {
                            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                                    new TransactionMethod<Object>() {
                                        @Override
                                        public Object runInTransaction() {
                                            HandleVdsInformation();
                                            vm.setRunOnVds(getVdsId());
                                            DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
                                            return null;
                                        }
                                    });
                        } else {
                            ResourceManager.getInstance().RemoveAsyncRunningVm(getParameters().getVmId());
                        }
                    }
                }
                getVDSReturnValue().setReturnValue(vm.getStatus());
            } else {
                getVDSReturnValue().setSucceeded(false);
            }
        } catch (java.lang.Exception e) {
            log.error("Error in excuting CreateVmVDSCommand", e);
            if (command == null || !command.getVDSReturnValue().getSucceeded()) {
                ResourceManager.getInstance().RemoveAsyncRunningVm(getParameters().getVmId());
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * @param vm
     * @return
     */
    private boolean isSysprepUsed(final VM vm) {
        return vm.useSysPrep() && vm.getVmOs().isWindows()
                && StringUtils.isEmpty(vm.getFloppyPath());
    }

    private void HandleVdsInformation() {
        getVds().setMemCommited(getVds().getMemCommited() + getParameters().getVm().getVmMemSizeMb());
        getVds().setMemCommited(getVds().getMemCommited() + getVds().getGuestOverhead());
        getVds().setVmCount(getVds().getVmCount() + 1);
        getVds().setVmsCoresCount(getVds().getVmsCoresCount() + getParameters().getVm().getNumOfCpus());
        getVds().setPendingVcpusCount(
                getVds().getPendingVcpusCount() + getParameters().getVm().getNumOfCpus());
        getVds().setPendingVmemSize(
                getVds().getPendingVmemSize() + getParameters().getVm().getMinAllocatedMem());
        log.infoFormat("IncreasePendingVms::CreateVmIncreasing vds {0} pending vcpu count, now {1}. Vm: {2}", getVds()
                .getName(), getVds().getPendingVcpusCount(), getParameters().getVm().getName());
        _vdsManager.UpdateDynamicData(getVds().getDynamicData());
    }

    private boolean CanExecute() {

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

    private void HandleCommandResult(CreateVDSCommand<?> command) {
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

    private static Log log = LogFactory.getLog(CreateVmVDSCommand.class);
}
