package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute(fieldName = "VmId")
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmCommand<T extends RemoveVmParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -3202434016040084728L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemoveVmCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
    }

    private boolean hasImages;

    @Override
    protected void ExecuteVmCommand() {
        if (!CanRemoveVm(getVmId())) {
            return;
        }
        if (getVm().getstatus() != VMStatus.ImageLocked || !getParameters().getForce()) {
            VmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());
        }
        freeLock();
        setSucceeded(removeVm());
    }

    private boolean removeVm() {
        VM vm = getVm();
        Guid vmId = getVmId();
        VmHandler.updateDisksFromDb(vm);
        hasImages = vm.getDiskMap().size() > 0;

        setVm(DbFacade.getInstance().getVmDAO().getById(vmId));
        RemoveVmInSpm(vm.getstorage_pool_id(), vmId);
        if (!RemoveVmImages(null)) {
            return false;
        }

        if (!hasImages) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    RemoveVmFromDb();
                    return null;
                }
            });
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;
        List<String> messages = getReturnValue().getCanDoActionMessages();

        if (getVm() == null) {
            retVal = false;
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND.name());
        } else {
            retVal = super.canDoAction() && isUnlockedOrForced(messages) && CanRemoveVm(getVmId(), messages);
            setDescription(getVmName());
        }

        messages.add(VdcBllMessages.VAR__ACTION__REMOVE.toString());
        messages.add(VdcBllMessages.VAR__TYPE__VM.toString());
        return retVal;
    }

    private boolean isUnlockedOrForced(List<String> message) {
        boolean returnValue = true;
        if (getVm().getstatus() == VMStatus.ImageLocked && !getParameters().getForce()) {
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_LOCKED.toString());
            returnValue = false;
        }

        return returnValue;
    }

    public boolean CanRemoveVm(Guid vmId) {
        return !(IsVmRunning(vmId) || IsVmInPool(vmId));

    }

    public static boolean IsVmRunning(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDAO().getById(vmId);
        if (vm != null) {
            return VM.isStatusUpOrPaused(vm.getstatus()) || vm.getstatus() == VMStatus.Unknown;
        }
        return false;
    }

    public static boolean IsVmInPool(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDAO().getById(vmId);
        return vm != null && vm.getVmPoolId() != null;
    }

    public boolean CanRemoveVm(Guid vmId, List<String> message) {
        boolean returnValue = true;
        VM vm = DbFacade.getInstance().getVmDAO().getById(vmId);
        java.util.ArrayList<String> imagesMessages = new java.util.ArrayList<String>();
        if (vm == null) {
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST.toString());
            returnValue = false;
        } else if (IsVmRunning(vmId) || (vm.getstatus() == VMStatus.NotResponding)) {
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING.toString());
            returnValue = false;
        } else if (vm.getstatus() == VMStatus.Suspended) {
            message.add(VdcBllMessages.VM_CANNOT_REMOVE_VM_WHEN_STATUS_IS_NOT_DOWN.toString());
            returnValue = false;
        } else if (IsVmInPool(vmId)) {
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL.toString());
            returnValue = false;
        }
        // enable to remove vms without images
        else {
            ValidationResult vmDuringSnapshotResult =
                    new SnapshotsValidator().vmNotDuringSnapshot(vm.getId());
            if (!vmDuringSnapshotResult.isValid()) {
                message.add(vmDuringSnapshotResult.getMessage().name());
                returnValue = false;
            } else {
                List<DiskImage> vmImages = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vmId);
                if (vmImages.size() > 0
                        && !ImagesHandler.PerformImagesChecks(vm, imagesMessages, vm.getstorage_pool_id(), Guid.Empty,
                                false, !getParameters().getForce(), false, false,
                                getParameters().getForce(), false, true, true, vmImages)) {
                    message.addAll(imagesMessages);
                    returnValue = false;
                }
            }
        }
        if (returnValue && getParameters().getForce() && getVm().getstatus() == VMStatus.ImageLocked) {
            // we cannot force remove if there is running task
            if (AsyncTaskManager.getInstance().HasTasksByStoragePoolId(getVm().getstorage_pool_id())) {
                message.add(VdcBllMessages.VM_CANNOT_REMOVE_HAS_RUNNING_TASKS.toString());
                returnValue = false;
            }
        }
        return returnValue;
    }

    protected boolean RemoveVmImages(java.util.ArrayList<DiskImage> images) {
        RemoveAllVmImagesParameters tempVar = new RemoveAllVmImagesParameters(getVmId(), images);
        tempVar.setParentCommand(getActionType());
        tempVar.setEntityId(getParameters().getEntityId());
        tempVar.setParentParemeters(getParameters());
        VdcReturnValueBase vdcRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.RemoveAllVmImages,
                        tempVar,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

        if (vdcRetValue.getSucceeded()) {
            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }

        return vdcRetValue.getSucceeded();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (hasImages) {
                return getSucceeded() ? AuditLogType.USER_REMOVE_VM : AuditLogType.USER_FAILED_REMOVE_VM;
            } else {
                return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FINISHED : AuditLogType.USER_FAILED_REMOVE_VM;
            }
        case END_FAILURE:
        case END_SUCCESS:
        default:
            return AuditLogType.USER_REMOVE_VM_FINISHED;
        }
    }

    protected void RemoveVmFromDb() {
        RemoveVmUsers();
        RemoveVmNetwork();
        // \\RemoveVmStatistics();
        // \\RemoveVmDynamic();
        new SnapshotsManager().removeSnapshots(getVmId());
        RemoveVmStatic();
    }

    @Override
    protected void EndVmCommand() {
        try {
            if (acquireLock()) {
                // Ensures the lock on the VM guid can be acquired. This prevents a race
                // between ExecuteVmCommand (for example, of a first multiple VMs removal that includes VM A,
                // and a second multiple VMs removal that include the same VM).
                setVm(DbFacade.getInstance().getVmDAO().getById(getVmId()));
                if (getVm() != null) {
                    VmHandler.UnLockVm(getVmId());
                    RemoveVmFromDb();
                }
            }
            setSucceeded(true);
        } finally {
            freeLock();
        }
    }
}
