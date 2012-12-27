package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmCommand<T extends RemoveVmParameters> extends VmCommand<T> implements QuotaStorageDependent{

    private static final long serialVersionUID = -3202434016040084728L;
    private boolean hasImages;
    private final List<String> disksLeftInVm = new ArrayList<String>();

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
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
        }
    }

    @Override
    protected void executeVmCommand() {
        if (getVm().getStatus() != VMStatus.ImageLocked) {
            VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
        }
        freeLock();
        setSucceeded(removeVm());
    }

    private boolean removeVm() {
        VM vm = getVm();
        Guid vmId = getVmId();
        hasImages = vm.getDiskList().size() > 0;

        removeVmInSpm(vm.getStoragePoolId(), vmId);
        if (hasImages && !removeVmImages(null)) {
            return false;
        }

        if (!hasImages) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    removeVmFromDb();
                    return null;
                }
            });
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().isDeleteProtected()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
        }

        return (super.canDoAction() && canRemoveVm());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    public static boolean IsVmRunning(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        if (vm != null) {
            return VM.isStatusUpOrPaused(vm.getStatus()) || vm.getStatus() == VMStatus.Unknown;
        }
        return false;
    }

    private boolean isVmInPool(Guid vmId) {
        return getVm().getVmPoolId() != null;
    }

    private boolean canRemoveVm() {
        if (IsVmRunning(getVmId()) || (getVm().getStatus() == VMStatus.NotResponding)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }
        if (getVm().getStatus() == VMStatus.Suspended) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_VM_WHEN_STATUS_IS_NOT_DOWN);
        }
        if (isVmInPool(getVmId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        }

        // enable to remove vms without images
        ValidationResult vmDuringSnapshotResult =
                new SnapshotsValidator().vmNotDuringSnapshot(getVm().getId());
        if (!vmDuringSnapshotResult.isValid()) {
            return failCanDoAction(vmDuringSnapshotResult.getMessage());
        }

        VmHandler.updateDisksFromDb(getVm());
        if (!ImagesHandler.PerformImagesChecks(getVm(),
                getReturnValue().getCanDoActionMessages(),
                getVm().getStoragePoolId(),
                Guid.Empty,
                false,
                !getParameters().getForce(),
                false,
                false,
                getParameters().getForce(),
                false,
                !getVm().getDiskMap().values().isEmpty(),
                true,
                getVm().getDiskMap().values())) {
            return false;
        }

        // we cannot force remove if there is running task
        if (getParameters().getForce() && getVm().getStatus() == VMStatus.ImageLocked
                && AsyncTaskManager.getInstance().HasTasksByStoragePoolId(getVm().getStoragePoolId())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_HAS_RUNNING_TASKS);
        }

        return true;
    }

    protected boolean removeVmImages(List<DiskImage> images) {
        RemoveAllVmImagesParameters tempVar = new RemoveAllVmImagesParameters(getVmId(), images);
        tempVar.setParentCommand(getActionType());
        tempVar.setEntityId(getParameters().getEntityId());
        tempVar.setParentParameters(getParameters());
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
            return disksLeftInVm.isEmpty() ? AuditLogType.USER_REMOVE_VM_FINISHED
                    : AuditLogType.USER_REMOVE_VM_FINISHED_WITH_ILLEGAL_DISKS;
        }
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(), LockingGroup.VM.name());
    }

    protected void removeVmFromDb() {
        removeLunDisks();
        removeVmUsers();
        removeVmNetwork();
        new SnapshotsManager().removeSnapshots(getVmId());
        removeVmStatic();
    }

    /**
     * The following method will perform a removing of all lunDisks from vm.
     * These is only DB operation
     */
    private void removeLunDisks() {
        List<LunDisk> lunDisks =
                ImagesHandler.filterDiskBasedOnLuns(getVm().getDiskMap().values());
        for (LunDisk lunDisk : lunDisks) {
            ImagesHandler.removeLunDisk(lunDisk);
        }
    }

    @Override
    protected void endVmCommand() {
        try {
            if (acquireLock()) {
                // Ensures the lock on the VM guid can be acquired. This prevents a race
                // between executeVmCommand (for example, of a first multiple VMs removal that includes VM A,
                // and a second multiple VMs removal that include the same VM).
                setVm(DbFacade.getInstance().getVmDao().get(getVmId()));
                if (getVm() != null) {
                    updateDisksAfterVmRemoved();

                    // Remove VM from DB.
                    removeVmFromDb();
                }
            }
            setSucceeded(true);
        } finally {
            freeLock();
        }
    }

    /**
     * Update disks for VM after disks were removed.
     */
    private void updateDisksAfterVmRemoved() {
        VmHandler.updateDisksFromDb(getVm());

        // Get all disk images for VM (VM should not have any image disk associated with it).
        List<DiskImage> diskImages = ImagesHandler.filterImageDisks(getVm().getDiskMap().values(),
                true,
                false);

        // If the VM still has disk images related to it, change their status to Illegal.
        if (!diskImages.isEmpty()) {
            for (DiskImage diskImage : diskImages) {
                disksLeftInVm.add(diskImage.getDiskAlias());
            }
            AddCustomValue("DisksNames", StringUtils.join(disksLeftInVm, ","));
        }
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : getVm().getDiskList()){
            if (disk.getQuotaId() != null && !Guid.Empty.equals(disk.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                        disk.getstorage_ids().get(0),
                        (double)disk.getSizeInGigabytes()));
            }
        }
        return list;
    }
}
