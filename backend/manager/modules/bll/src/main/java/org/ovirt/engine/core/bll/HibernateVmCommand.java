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
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class HibernateVmCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    private static final String SAVE_IMAGE_TASK_KEY = "SAVE_IMAGE_TASK_KEY";
    private static final String SAVE_RAM_STATE_TASK_KEY = "SAVE_RAM_STATE_TASK_KEY";
    private boolean isHibernateVdsProblematic = false;

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
        setStoragePoolId(getVm().getStoragePoolId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private Guid _storageDomainId = Guid.Empty;

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), SAVE_IMAGE_TASK_KEY);
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), SAVE_RAM_STATE_TASK_KEY);
    }

    /*
     * find a storage domain to store the hibernation volumes
     * domain must:
     *     be data domain (or master)
     *     be active
     *     have enough space for the volumes
     * return Guid.Empty if no domain found
     */
    @Override
    public Guid getStorageDomainId() {
        if (_storageDomainId.equals(Guid.Empty) && getVm() != null) {
            List<DiskImage> diskDummiesForMemSize = MemoryUtils.createDiskDummies(getVm().getTotalMemorySizeInBytes(),
                    MemoryUtils.META_DATA_SIZE_IN_BYTES);
            StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(getVm().getStoragePoolId(), diskDummiesForMemSize);
            if (storageDomain != null) {
                _storageDomainId = storageDomain.getId();
            }
        }
        return _storageDomainId;
    }

    @Override
    protected void perform() {
        // Set the VM to null, to fetch it again from the DB ,instead from the cache.
        // We want to get the VM state from the DB, to avoid multi requests for VM hibernation.
        setVm(null);
        if (getVm().isRunning()) {

            TransactionSupport.executeInNewTransaction(
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getCompensationContext().snapshotEntityStatus(getVm().getDynamicData());

                            // Set the VM to SavingState to lock the VM,to avoid situation of multi VM hibernation.
                            getVm().setStatus(VMStatus.PreparingForHibernate);

                            runVdsCommand(VDSCommandType.UpdateVmDynamicData,
                                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                                    getVm().getDynamicData()));
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });

            final Guid taskId1 = getAsyncTaskId(SAVE_IMAGE_TASK_KEY);

            Guid image1GroupId = Guid.newGuid();
            // this is temp code until SPM will implement the new verb that does
            // it for us:

            Guid hiberVol1 = Guid.newGuid();
            final VDSReturnValue ret1 =
                    runVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(
                                            getVm().getStoragePoolId(),
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

            Guid guid1 = TransactionSupport.executeInNewTransaction(
                    new TransactionMethod<Guid>() {
                        @Override
                        public Guid runInTransaction() {
                            getCompensationContext().resetCompensation();
                            return createTaskInCurrentTransaction(
                                    taskId1,
                                    ret1.getCreationInfo(),
                                    VdcActionType.HibernateVm,
                                    VdcObjectType.Storage,
                                    getStorageDomainId());
                        }
                    });

            getReturnValue().getVdsmTaskIdList().add(guid1);

            Guid taskId2 = getAsyncTaskId(SAVE_RAM_STATE_TASK_KEY);

            // second vol should be 10kb
            Guid image2GroupId = Guid.newGuid();

            Guid hiberVol2 = Guid.newGuid();
            VDSReturnValue ret2 =
                    runVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(getVm().getStoragePoolId(),
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

            // this is the new param that should be passed to the hibernate
            // command
            getVm().setHibernationVolHandle(MemoryUtils.createMemoryStateString(
                    getStorageDomainId(), getVm().getStoragePoolId(),
                    image1GroupId, hiberVol1, image2GroupId, hiberVol2));
            // end of temp code

            runVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                    getVm().getDynamicData()));

            getParameters().setVdsmTaskIds(new ArrayList<Guid>(getReturnValue().getVdsmTaskIdList()));

            setSucceeded(true);
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    /**
     * Note: the treatment for {@link CommandActionState#END_SUCCESS} is the same as for {@link CommandActionState#END_FAILURE}
     * because if after calling {@link HibernateVmCommand#endSuccessfully()} the method {@link HibernateVmCommand#getSucceeded()}
     * returns true, the command is set not to be logged and this method is not called
     *
     * @see {@link HibernateVmCommand#endSuccessfully()}
     */
    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_SUSPEND_VM : AuditLogType.USER_FAILED_SUSPEND_VM;
        case END_SUCCESS:
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

        if (getStorageDomainId().equals(Guid.Empty)) {
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
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void endSuccessfully() {
        endSuccessfullyImpl();
        // no event should be displayed if the command ended successfully
        setCommandShouldBeLogged(!getSucceeded());
    }

    private void endSuccessfullyImpl() {
        if (getVm() != null) {
            if (getVm().getStatus() != VMStatus.PreparingForHibernate) {
                // If the Vm is not PreparingForHibernate, we shouldn't perform Hibernate on it,
                // since if the Vm is in another status, something might have happened to it
                // that might prevent it from being hibernated.

                // NOTE: We don't remove the 2 volumes because we don't want to
                // start here another tasks.

                log.warnFormat(
                        "VM '{0}' is not in 'PreparingForHibernate' status, but in '{1}' status - not performing Hibernate.",
                        getVm().getName(),
                        getVm().getStatus());
                getReturnValue().setEndActionTryAgain(false);
            }

            else if (getVm().getRunOnVds() == null) {
                log.warnFormat(
                        "VM '{0}' doesn't have 'run_on_vds' value - cannot Hibernate.",
                        getVm().getName());
                getReturnValue().setEndActionTryAgain(false);
            }

            else {
                String hiberVol = getVm().getHibernationVolHandle();
                if (hiberVol != null) {
                    try {
                        runVdsCommand(
                                        VDSCommandType.Hibernate,
                                        new HibernateVDSCommandParameters(new Guid(getVm().getRunOnVds().toString()),
                                                getVmId(), getVm().getHibernationVolHandle()));
                    } catch (VdcBLLException e) {
                        isHibernateVdsProblematic = true;
                        throw e;
                    }
                    setSucceeded(true);
                } else {
                    log.errorFormat("hibernation volume of VM '{0}', is not initialized.", getVm().getName());
                    endWithFailure();
                }
            }
        }

        else {
            log.warn("VM is null - not performing full endAction.");
            setSucceeded(true);
        }
    }

    @Override
    protected void endWithFailure() {
        if (getVm() != null) {
            revertTasks();
            if (getVm().getRunOnVds() != null) {
                getVm().setHibernationVolHandle(null);
                getVm().setStatus(VMStatus.Up);

                runVdsCommand(
                                VDSCommandType.UpdateVmDynamicData,
                                new UpdateVmDynamicDataVDSCommandParameters(
                                        new Guid(getVm().getRunOnVds().toString()), getVm().getDynamicData()));

                setSucceeded(true);
            }

            else {
                log.warnFormat(
                        "VM '{0}' doesn't have 'run_on_vds' value - not clearing 'hibernation_vol_handle' info.",
                        getVm().getName());

                getReturnValue().setEndActionTryAgain(false);
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("VM is null - not performing full endAction.");
            setSucceeded(true);
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

    private static final Log log = LogFactory.getLog(HibernateVmCommand.class);
}
