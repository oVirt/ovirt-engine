package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class HibernateVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(HibernateVmCommand.class);

    private static final String SAVE_IMAGE_TASK_KEY = "SAVE_IMAGE_TASK_KEY";
    private static final String SAVE_RAM_STATE_TASK_KEY = "SAVE_RAM_STATE_TASK_KEY";

    private boolean isHibernateVdsProblematic;
    private Guid cachedStorageDomainId;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected HibernateVmCommand(Guid commandId) {
        super(commandId);
    }

    public HibernateVmCommand(T parameters) {
        super(parameters);
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
            parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), SAVE_IMAGE_TASK_KEY);
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), SAVE_RAM_STATE_TASK_KEY);
    }

    /**
     * Finds an active data/master storage domain which has enough space to store the hibernation volumes
     *
     * @return storage domain id or null if no suitable storage domain exists
     */
    @Override
    public Guid getStorageDomainId() {
        if (cachedStorageDomainId == null) {
            List<DiskImage> diskDummiesForMemSize = MemoryUtils.createDiskDummies(
                    getVm().getTotalMemorySizeInBytes(),
                    MemoryUtils.META_DATA_SIZE_IN_BYTES);
            StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(getStoragePoolId(), diskDummiesForMemSize);
            if (storageDomain != null) {
                cachedStorageDomainId = storageDomain.getId();
            }
        }
        return cachedStorageDomainId;
    }

    @Override
    protected void perform() {
        final Guid taskId1 = getAsyncTaskId(SAVE_IMAGE_TASK_KEY);

        Guid image1GroupId = Guid.newGuid();

        Guid hiberVol1 = Guid.newGuid();
        final VDSReturnValue ret1 =
                runVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(
                                getStoragePoolId(),
                                getStorageDomainId(),
                                image1GroupId,
                                getVm().getTotalMemorySizeInBytes(),
                                getMemoryVolumeType(),
                                VolumeFormat.RAW,
                                hiberVol1,
                                ""));

        if (!ret1.getSucceeded()) {
            return;
        }

        Guid guid1 = createTask(taskId1, ret1.getCreationInfo(), VdcActionType.HibernateVm);
        getReturnValue().getVdsmTaskIdList().add(guid1);

        Guid taskId2 = getAsyncTaskId(SAVE_RAM_STATE_TASK_KEY);

        // second vol should be 10kb
        Guid image2GroupId = Guid.newGuid();

        Guid hiberVol2 = Guid.newGuid();
        VDSReturnValue ret2 =
                runVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(getStoragePoolId(),
                                getStorageDomainId(),
                                image2GroupId,
                                MemoryUtils.META_DATA_SIZE_IN_BYTES,
                                VolumeType.Sparse,
                                VolumeFormat.COW,
                                hiberVol2,
                                ""));

        if (!ret2.getSucceeded()) {
            return;
        }
        Guid guid2 = createTask(taskId2, ret2.getCreationInfo(), VdcActionType.HibernateVm);
        getReturnValue().getVdsmTaskIdList().add(guid2);

        getSnapshotDAO().updateHibernationMemory(getVmId(),
                MemoryUtils.createMemoryStateString(
                        getStorageDomainId(), getStoragePoolId(),
                        image1GroupId, hiberVol1, image2GroupId, hiberVol2));

        getParameters().setVdsmTaskIds(new ArrayList<Guid>(getReturnValue().getVdsmTaskIdList()));

        setSucceeded(true);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_SUSPEND_VM : AuditLogType.USER_FAILED_SUSPEND_VM;
        case END_SUCCESS:
            if (getSucceeded()) {
                // no event should be displayed if the command ended successfully, the monitoring will log it
                return AuditLogType.UNASSIGNED;
            }
        case END_FAILURE:
        default:
            return isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN
                    : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isSuspendSupportedByArchitecture(getVm().getClusterArch(),
                getVm().getVdsGroupCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SUSPEND_NOT_SUPPORTED);
        }

        if (!canRunActionOnNonManagedVm()) {
           return false;
        }

        VMStatus vmStatus = getVm().getStatus();
        if (vmStatus == VMStatus.WaitForLaunch || vmStatus == VMStatus.NotResponding) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(vmStatus));
        }

        if (vmStatus != VMStatus.Up) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_UP);
        }

        if (CommandCoordinatorUtil.entityHasTasks(getVmId())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS);
        }

        if (getVm().getVmPoolId() != null) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_SUSPEND_VM_FROM_POOL);
        }

        // check if vm has stateless images in db in case vm was run once as stateless
        // (then isStateless is false)
        if (getVm().isStateless() ||
                DbFacade.getInstance().getSnapshotDao().exists(getVmId(), SnapshotType.STATELESS)) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_SUSPEND_STATELESS_VM);
        }

        if (getStorageDomainId() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HIBERNATE);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getVmIsHibernatingMessage()));
    }

    private String getVmIsHibernatingMessage() {
        StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_HIBERNATING.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    @Override
    protected void endSuccessfully() {
        if (getVm().getRunOnVds() == null) {
            log.warn(
                    "VM '{}' doesn't have 'run_on_vds' value - cannot Hibernate.",
                    getVm().getName());
            getReturnValue().setEndActionTryAgain(false);
        }

        else {
            String hiberVol = getActiveSnapshot().getMemoryVolume();
            if (hiberVol != null) {
                try {
                    runVdsCommand(VDSCommandType.Hibernate,
                            new HibernateVDSCommandParameters(getVm().getRunOnVds(), getVmId(), hiberVol));
                } catch (VdcBLLException e) {
                    isHibernateVdsProblematic = true;
                    throw e;
                }
                setSucceeded(true);
            } else {
                log.error("Hibernation volume of VM '{}', is not initialized.", getVm().getName());
                endWithFailure();
            }
        }
    }

    @Override
    protected void endWithFailure() {
        revertTasks();
        if (getVm().getRunOnVds() != null) {
            getSnapshotDAO().removeMemoryFromActiveSnapshot(getVmId());
            setSucceeded(true);
        }

        else {
            log.warn(
                    "VM '{}' doesn't have 'run_on_vds' value - not clearing 'hibernation_vol_handle' info.",
                    getVm().getName());

            getReturnValue().setEndActionTryAgain(false);
        }
    }

    private VolumeType getMemoryVolumeType() {
        return getMemoryVolumeTypeForStorageDomain(getStorageDomain().getStorageType());
    }

    /**
     * Returns whether to use Sparse or Preallocation. If the storage type is file system devices ,it would be more
     * efficient to use Sparse allocation. Otherwise for block devices we should use Preallocated for faster allocation.
     *
     * @return - VolumeType of allocation type to use.
     */
    public static VolumeType getMemoryVolumeTypeForStorageDomain(StorageType storageType) {
        return storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
    }
}
