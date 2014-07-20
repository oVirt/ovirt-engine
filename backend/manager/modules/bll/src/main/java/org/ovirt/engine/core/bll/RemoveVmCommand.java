package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.network.ExternalNetworkManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveVmCommand<T extends RemoveVmParameters> extends VmCommand<T> implements QuotaStorageDependent, TaskHandlerCommand<RemoveVmParameters> {

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
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
        }
    }

    @Override
    protected void executeVmCommand() {
        if (getVm().getStatus() != VMStatus.ImageLocked) {
            VmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
        }
        freeLock();
        setSucceeded(removeVm());
    }

    @Override
    protected boolean shouldRemoveMemorySnapshotVolumes(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 0;
    }

    private boolean removeVm() {
        final List<DiskImage> diskImages = ImagesHandler.filterImageDisks(getVm().getDiskList(),
                true,
                false,
                true);

        for (VmNic nic : getInterfaces()) {
            new ExternalNetworkManager(nic).deallocateIfExternal();
        }

        removeMemoryVolumes();

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                removeVmFromDb();
                if (getParameters().isRemoveDisks()) {
                    for (DiskImage image : diskImages) {
                        getCompensationContext().snapshotEntityStatus(image.getImage(), ImageStatus.ILLEGAL);
                        ImagesHandler.updateImageStatus(image.getImage().getId(), ImageStatus.LOCKED);
                    }
                    getCompensationContext().stateChanged();
                }
                else {
                    for (DiskImage image : diskImages) {
                        getImageDao().updateImageVmSnapshotId(image.getImageId(), null);
                    }
                }
                return null;
            }
        });

        if (getParameters().isRemoveDisks() && !diskImages.isEmpty()) {
            Collection<DiskImage> unremovedDisks = (Collection<DiskImage>)removeVmImages(diskImages).getActionReturnValue();
            if (!unremovedDisks.isEmpty()) {
                processUnremovedDisks(unremovedDisks);
                return false;
            }
        }

        return true;
    }

    private void removeMemoryVolumes() {
        Set<String> memoryStates =
                MemoryUtils.getMemoryVolumesFromSnapshots(getDbFacade().getSnapshotDao().getAll(getVmId()));
        for (String memoryState : memoryStates) {
            VdcReturnValueBase retVal = getBackend().runInternalAction(
                    VdcActionType.RemoveMemoryVolumes,
                    buildRemoveMemoryVolumesParameters(memoryState, getVmId()));

            if (!retVal.getSucceeded()) {
                log.errorFormat("Failed to remove memory volumes whie removing vm {0} (volumes: {1})",
                        getVmId(), memoryState);
            }
        }
    }

    private RemoveMemoryVolumesParameters buildRemoveMemoryVolumesParameters(String memoryState, Guid vmId) {
        RemoveMemoryVolumesParameters parameters =
                new RemoveMemoryVolumesParameters(memoryState, getVmId());
        parameters.setRemoveOnlyIfNotUsedAtAll(true);

        return parameters;
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().isDeleteProtected()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DELETE_PROTECTION_ENABLED);
        }

        VmHandler.updateDisksFromDb(getVm());

        if (!getParameters().isRemoveDisks() && !canRemoveVmWithDetachDisks()) {
            return false;
        }

        return (super.canDoAction() && canRemoveVm());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    public static boolean isVmRunning(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        if (vm != null) {
            return vm.isRunningOrPaused() || vm.getStatus() == VMStatus.Unknown;
        }
        return false;
    }

    private boolean isVmInPool(Guid vmId) {
        return getVm().getVmPoolId() != null;
    }

    private boolean canRemoveVm() {
        if (isVmRunning(getVmId()) || (getVm().getStatus() == VMStatus.NotResponding)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }
        if (getVm().getStatus() == VMStatus.Suspended) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_VM_WHEN_STATUS_IS_NOT_DOWN);
        }
        if (isVmInPool(getVmId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_ATTACHED_TO_POOL);
        }

        // enable to remove vms without images
        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        if (!validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))) {
            return false;
        }

        if (getParameters().getForce() && !validate(snapshotsValidator.vmNotInPreview(getVmId()))) {
            return false;
        }

        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        Collection<Disk> vmDisks = getVm().getDiskMap().values();
        List<DiskImage> vmImages = ImagesHandler.filterImageDisks(vmDisks, true, false, true);
        if (!vmImages.isEmpty()) {
            Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(vmImages);
            MultipleStorageDomainsValidator storageValidator = new MultipleStorageDomainsValidator(getVm().getStoragePoolId(), storageIds);
            if (!validate(storageValidator.allDomainsExistAndActive())) {
                return false;
            }

            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(vmImages);
            if (!getParameters().getForce() && !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }
        }

        // Handle VM status with ImageLocked
        VmValidator vmValidator = new VmValidator(getVm());
        ValidationResult vmLockedValidatorResult = vmValidator.vmNotLocked();
        if (!vmLockedValidatorResult.isValid()) {
            // without force remove, we can't remove the VM
            if (!getParameters().getForce()) {
                return failCanDoAction(vmLockedValidatorResult.getMessage());
            }

            // If it is force, we cannot remove if there are task
            if (AsyncTaskManager.getInstance().hasTasksByStoragePoolId(getVm().getStoragePoolId())) {
                return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_HAS_RUNNING_TASKS);
            }
        }

        if (getParameters().isRemoveDisks() && !validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false))) {
            return false;
        }

        return true;
    }

    private boolean canRemoveVmWithDetachDisks() {
        if (!Guid.Empty.equals(getVm().getVmtGuid())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_WITH_DETACH_DISKS_BASED_ON_TEMPLATE);
        }

        for (Disk disk : getVm().getDiskList()) {
            List<DiskImage> diskImageList = getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId());
            if (diskImageList.size() > 1) {
                return failCanDoAction(VdcBllMessages.VM_CANNOT_REMOVE_WITH_DETACH_DISKS_SNAPSHOTS_EXIST);
            }
        }

        return true;
    }

    protected VdcReturnValueBase removeVmImages(List<DiskImage> images) {
        VdcReturnValueBase vdcRetValue =
                Backend.getInstance().runInternalAction(VdcActionType.RemoveAllVmImages,
                        buildRemoveAllVmImagesParameters(images),
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

        if (vdcRetValue.getSucceeded()) {
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }

        return vdcRetValue;
    }

    private RemoveAllVmImagesParameters buildRemoveAllVmImagesParameters(List<DiskImage> images) {
        RemoveAllVmImagesParameters params = new RemoveAllVmImagesParameters(getVmId(), images);
        if (getParameters().getParentCommand() == VdcActionType.Unknown) {
            params.setParentCommand(getActionType());
            params.setEntityInfo(getParameters().getEntityInfo());
            params.setParentParameters(getParameters());
        } else {
            params.setParentCommand(getParameters().getParentCommand());
            params.setEntityInfo(getParameters().getParentParameters().getEntityInfo());
            params.setParentParameters(getParameters().getParentParameters());
        }

        return params;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FINISHED : AuditLogType.USER_REMOVE_VM_FINISHED_WITH_ILLEGAL_DISKS;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected void removeVmFromDb() {
        removeLunDisks();
        removeVmUsers();
        removeVmNetwork();
        removeVmSnapshots();
        removeVmStatic(getParameters().isRemovePermissions());
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
        // no audit log print here as the vm was already removed during the execute phase.
        setCommandShouldBeLogged(false);

        setSucceeded(true);
    }

    private void processUnremovedDisks(Collection<DiskImage> diskImages) {
        List<String> disksLeftInVm = new ArrayList<String>();
        for (DiskImage diskImage : diskImages) {
            disksLeftInVm.add(diskImage.getDiskAlias());
        }
        addCustomValue("DisksNames", StringUtils.join(disksLeftInVm, ","));
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        if (getParameters().isRemoveDisks()) {
            List<QuotaConsumptionParameter> list = new ArrayList<>();
            ImagesHandler.fillImagesBySnapshots(getVm());
            for (DiskImage disk : getVm().getDiskList()) {
                for (DiskImage snapshot : disk.getSnapshots()) {
                    if (snapshot.getQuotaId() != null && !Guid.Empty.equals(snapshot.getQuotaId())) {
                        if (snapshot.getActive()) {
                            list.add(new QuotaStorageConsumptionParameter(
                                    snapshot.getQuotaId(),
                                    null,
                                    QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                                    disk.getStorageIds().get(0),
                                    (double) snapshot.getSizeInGigabytes()));
                        } else {
                            list.add(new QuotaStorageConsumptionParameter(
                                    snapshot.getQuotaId(),
                                    null,
                                    QuotaStorageConsumptionParameter.QuotaAction.RELEASE,
                                    disk.getStorageIds().get(0),
                                    snapshot.getActualSize()));
                        }
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    ///////////////////////////////////////
    // TaskHandlerCommand Implementation //
    ///////////////////////////////////////

    public T getParameters() {
        return super.getParameters();
    }

    public VdcActionType getActionType() {
        return super.getActionType();
    }

    public VdcReturnValueBase getReturnValue() {
        return super.getReturnValue();
    }

    public ExecutionContext getExecutionContext() {
        return super.getExecutionContext();
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        super.setExecutionContext(executionContext);
    }

    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTaskInCurrentTransaction(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    public void preventRollback() {
        throw new NotImplementedException();
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }
}
