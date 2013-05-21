package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.LiveSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.NullableMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.StatelessSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> implements QuotaStorageDependent, TaskHandlerCommand<CreateAllSnapshotsFromVmParameters> {

    private List<DiskImage> cachedSelectedActiveDisks;
    private Guid cachedStorageDomainId = Guid.Empty;
    /** flag that indicates whether memory snapshot is about to be created */
    private boolean prepareForMemorySnapshot;
    private String cachedSnapshotIsBeingTakenMessage;

    protected CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
        setSnapshotName(parameters.getDescription());
        setStoragePoolId(getVm() != null ? getVm().getStoragePoolId() : null);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getParameters().getDescription());
        }
        return jobProperties;
    }

    /**
     * Filter all allowed snapshot disks.
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            cachedSelectedActiveDisks = ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()),
                    true,
                    true);
        }
        return cachedSelectedActiveDisks;
    }

    private void incrementVmGeneration() {
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
    }

    @Override
    protected void executeVmCommand() {
        Guid newActiveSnapshotId = Guid.NewGuid();
        Guid createdSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        getParameters().setSnapshotType(determineSnapshotType());
        getParameters().setInitialVmStatus(getVm().getStatus());

        getSnapshotDao().updateId(createdSnapshotId, newActiveSnapshotId);

        setActionReturnValue(createdSnapshotId);

        MemoryImageBuilder memoryImageBuilder = createMemoryImageBuilder();
        addSnapshotToDB(createdSnapshotId, memoryImageBuilder);
        createSnapshotsForDisks(newActiveSnapshotId);
        memoryImageBuilder.build();

        if (getTaskIdList().isEmpty()) {
            getParameters().setTaskGroupSuccess(true);
            incrementVmGeneration();
        }

        if (!prepareForMemorySnapshot) {
            freeLock();
        }

        setSucceeded(true);
    }

    public Guid getStorageDomainIdForVmMemory() {
        if (cachedStorageDomainId.equals(Guid.Empty) && getVm() != null) {
            long sizeNeeded = getVm().getTotalMemorySizeInBytes() / BYTES_IN_GB;
            StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(getVm().getStoragePoolId(), sizeNeeded);
            if (storageDomain != null) {
                cachedStorageDomainId = storageDomain.getId();
            }
        }
        return cachedStorageDomainId;
    }

    private MemoryImageBuilder createMemoryImageBuilder() {
        if (!FeatureSupported.memorySnapshot(getVm().getVdsGroupCompatibilityVersion())) {
            return new NullableMemoryImageBuilder();
        }

        if (getParameters().getSnapshotType() == SnapshotType.STATELESS) {
            return new StatelessSnapshotMemoryImageBuilder(getVm());
        }

        if (getParameters().isSaveMemory() && isLiveSnapshotApplicable()) {
            prepareForMemorySnapshot = true;
            return new LiveSnapshotMemoryImageBuilder(getVm(), getStorageDomainIdForVmMemory(), getStoragePool(), this);
        }

        return new NullableMemoryImageBuilder();
    }

    private Snapshot addSnapshotToDB(Guid snapshotId, MemoryImageBuilder memoryImageBuilder) {
        boolean taskExists = !getDisksList().isEmpty() || memoryImageBuilder.isCreateTasks();
        return new SnapshotsManager().addSnapshot(snapshotId,
                getParameters().getDescription(),
                taskExists ? SnapshotStatus.LOCKED : SnapshotStatus.OK,
                getParameters().getSnapshotType(),
                getVm(),
                true,
                memoryImageBuilder.getVolumeStringRepresentation(),
                getCompensationContext());
    }

    private void createSnapshotsForDisks(Guid vmSnapshotId) {
        for (DiskImage image : getDisksList()) {

            VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                            VdcActionType.CreateSnapshot,
                            buildCreateSnapshotParameters(image, vmSnapshotId),
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

            if (vdcReturnValue.getSucceeded()) {
                getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
            } else {
                throw new VdcBLLException(vdcReturnValue.getFault().getError(),
                        "Failed to create snapshot!");
            }
        }
    }

    private ImagesActionsParametersBase buildCreateSnapshotParameters(DiskImage image, Guid snapshotId) {
        VdcActionType parentCommand = getParameters().getParentCommand() != VdcActionType.Unknown ?
                getParameters().getParentCommand() : VdcActionType.CreateAllSnapshotsFromVm;

        ImagesActionsParametersBase result = new ImagesActionsParametersBase(image.getImageId());
        result.setDescription(getParameters().getDescription());
        result.setSessionId(getParameters().getSessionId());
        result.setQuotaId(image.getQuotaId());
        result.setVmSnapshotId(snapshotId);
        result.setEntityId(getParameters().getEntityId());
        result.setParentCommand(parentCommand);
        result.setParentParameters(getParametersForTask(parentCommand, getParameters()));
        return result;
    }

    /**
     * @return For internal execution, return the type from parameters, otherwise return {@link SnapshotType#REGULAR}.
     */
    protected SnapshotType determineSnapshotType() {
        return isInternalExecution() ? getParameters().getSnapshotType() : SnapshotType.REGULAR;
    }

    @Override
    protected void endVmCommand() {
        // The following code must be executed in an inner transaction to make the changes visible
        // to the RunVm command that might occur afterwards
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                final boolean taskGroupSucceeded = getParameters().getTaskGroupSuccess();
                Snapshot createdSnapshot = getSnapshotDao().get(getVmId(), getParameters().getSnapshotType(), SnapshotStatus.LOCKED);
                if (taskGroupSucceeded) {
                    getSnapshotDao().updateStatus(createdSnapshot.getId(), SnapshotStatus.OK);

                    if (isLiveSnapshotApplicable()) {
                        performLiveSnapshot(createdSnapshot);
                    }
                    else {
                        // If the created snapshot contains memory, remove the memory volumes as
                        // they are not in use since no live snapshot was created
                        String memoryVolume = createdSnapshot.getMemoryVolume();
                        if (!memoryVolume.isEmpty() &&
                                getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1) {
                            boolean succeed = removeMemoryVolumes(memoryVolume, getActionType(), false);
                            if (!succeed) {
                                log.errorFormat("Failed to remove unused memory {0} of snapshot {1}",
                                        memoryVolume, createdSnapshot.getId());
                            }
                        }
                    }
                } else {
                    revertToActiveSnapshot(createdSnapshot.getId());
                    // If the removed snapshot contained memory, remove the memory volumes
                    // Note that the memory volumes might not have been created
                    String memoryVolume = createdSnapshot.getMemoryVolume();
                    if (!memoryVolume.isEmpty() &&
                            getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1) {
                        boolean succeed = removeMemoryVolumes(memoryVolume, getActionType(), false);
                        if (!succeed) {
                            log.warnFormat("Failed to remove memory {0} of snapshot {1}",
                                    memoryVolume, createdSnapshot.getId());
                        }
                    }
                }

                incrementVmGeneration();

                endActionOnDisks();
                setSucceeded(taskGroupSucceeded);
                getReturnValue().setEndActionTryAgain(false);
                return null;
            }
        });

        // if the VM is HA + it went down during the snapshot creation process because of an error, run it
        // (during the snapshot creation process the VM couldn't be started (rerun))
        if (getVm() != null && getVm().isAutoStartup() && isVmDownUnintentionally() &&
                getParameters().getInitialVmStatus() != VMStatus.Down) {
            Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                    new RunVmParams(getVmId()),
                    ExecutionHandler.createInternalJobContext());
        }
    }

    private boolean isLiveSnapshotApplicable() {
        return getParameters().getParentCommand() != VdcActionType.RunVm && getVm() != null
                && getVm().isRunning() && getVm().getRunOnVds() != null;
    }

    @Override
    protected List<VdcActionParametersBase> getParametersForChildCommand() {
        List<VdcActionParametersBase> sortedList = getParameters().getImagesParameters();
        Collections.sort(sortedList, new Comparator<VdcActionParametersBase>() {
            @Override
            public int compare(VdcActionParametersBase o1, VdcActionParametersBase o2) {
                return ((ImagesActionsParametersBase) o1).getDestinationImageId()
                        .compareTo(((ImagesActionsParametersBase) o2).getDestinationImageId());
            }
        });

        return sortedList;
    }

    /**
     * returns true if the VM is down because of an error, otherwise false
     */
    private boolean isVmDownUnintentionally() {
        VM vm = getVmDAO().get(getVmId());
        return vm.getExitStatus() == VmExitStatus.Error && vm.isDown();
    }

    /**
     * Perform live snapshot on the host that the VM is running on. If the snapshot fails, and the error is
     * unrecoverable then the {@link CreateAllSnapshotsFromVmParameters#getTaskGroupSuccess()} will return false - which
     * will indicate that rollback of snapshot command should happen.
     *
     * @param createdSnapshotId
     *            Snapshot to revert to being active, in case of rollback.
     */
    protected void performLiveSnapshot(final Snapshot snapshot) {
        try {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {

                    runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(snapshot));

                    return null;
                }
            });
        } catch (VdcBLLException e) {
            handleVdsLiveSnapshotFailure(e);
        }
    }

    private SnapshotVDSCommandParameters buildLiveSnapshotParameters(Snapshot snapshot) {
        List<Disk> pluggedDisks = getDiskDao().getAllForVm(getVm().getId(), true);
        List<DiskImage> filteredPluggedDisks = ImagesHandler.filterImageDisks(pluggedDisks, false, true);
        if (FeatureSupported.memorySnapshot(getVm().getVdsGroupCompatibilityVersion())) {
            return new SnapshotVDSCommandParameters(getVm().getRunOnVds().getValue(),
                    getVm().getId(),
                    filteredPluggedDisks,
                    snapshot.getMemoryVolume());
        }
        else {
            return new SnapshotVDSCommandParameters(getVm().getRunOnVds().getValue(),
                    getVm().getId(),
                    filteredPluggedDisks);
        }
    }

    private void handleVdsLiveSnapshotFailure(VdcBLLException e) {
        log.warnFormat("Wasn't able to live snpashot due to error: {0}. VM will still be configured to the new created snapshot",
                ExceptionUtils.getMessage(e));
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        AuditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_FINISHED_FAILURE);
    }

    /**
     * Return the given snapshot ID's snapshot to be the active snapshot. The snapshot with the given ID is removed
     * in the process.
     *
     * @param createdSnapshotId
     *            The snapshot ID to return to being active.
     */
    protected void revertToActiveSnapshot(Guid createdSnapshotId) {
        if (createdSnapshotId != null) {
            getSnapshotDao().remove(createdSnapshotId);
            getSnapshotDao().updateId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE), createdSnapshotId);
        }
        setSucceeded(false);
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT : AuditLogType.USER_FAILED_CREATE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        // Initialize validators.
        VmValidator vmValidator = createVmValidator();
        SnapshotsValidator snapshotValidator = createSnapshotValidator();
        StoragePoolValidator spValidator = createStoragePoolValidator();

        if (!(validateVM(vmValidator) && validate(spValidator.isUp())
                && validate(vmValidator.vmNotIlegal())
                && validate(vmValidator.vmNotLocked())
                && validate(snapshotValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotValidator.vmNotInPreview(getVmId()))
                && validate(vmValidator.vmNotDuringMigration())
                && validate(vmValidator.vmNotRunningStateless()))) {
            return false;
        }

        List<DiskImage> disksList = getDisksList();
        if (disksList.size() > 0) {
            MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(disksList);
            DiskImagesValidator diskImagesValidator = createDiskImageValidator(disksList);
            if (!(validate(diskImagesValidator.diskImagesNotLocked())
                    && validate(diskImagesValidator.diskImagesNotIllegal())
                    && validate(sdValidator.allDomainsExistAndActive())
                    && validate(sdValidator.allDomainsWithinThresholds()))) {
                return false;
            }
        }

        if (getParameters().isSaveMemory() && Guid.Empty.equals(getStorageDomainIdForVmMemory())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
        }

        return true;
    }

    protected StoragePoolValidator createStoragePoolValidator() {
        return new StoragePoolValidator(getStoragePool());
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
    }

    protected DiskImagesValidator createDiskImageValidator(List<DiskImage> disksList) {
        return new DiskImagesValidator(disksList);
    }

    protected MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(List<DiskImage> disksList) {
        return new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksList));
    }

    protected VmValidator createVmValidator() {
        return new VmValidator(getVm());
    }

    protected boolean validateVM(VmValidator vmValidator) {
        return canDoSnapshot(getVm()) &&
                validate(vmValidator.vmNotSavingRestoring());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
    }

    private boolean canDoSnapshot(VM vm) {
        // if live snapshot is not available, then if vm is up - snapshot is not possible so it needs to be
        // checked if vm up or not
        // if live snapshot is enabled, there is no need to check if vm is up since in any case snapshot is possible
        if (!isLiveSnapshotEnabled() && !vm.isDown()) {
            // if there is no live snapshot and the vm is up - snapshot is not possible
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DATA_CENTER_VERSION_DOESNT_SUPPORT_LIVE_SNAPSHOT);
        }
        return true;
    }

    /**
     * @return If DC level does not support live snapshots.
     */
    private boolean isLiveSnapshotEnabled() {
        return Config.<Boolean> GetValue(
                ConfigValues.LiveSnapshotEnabled, getStoragePool().getcompatibility_version().getValue());
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshot;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getParameters().isNeedsLocking() ?
                Collections.singletonMap(getVmId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getSnapshotIsBeingTakenForVmMessage()))
                : null;
    }

    private String getSnapshotIsBeingTakenForVmMessage() {
        if (cachedSnapshotIsBeingTakenMessage == null) {
            StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_SNAPSHOT_IS_BEING_TAKEN_FOR_VM.name());
            if (getVmName() != null) {
                builder.append(String.format("$VmName %1$s", getVmName()));
            }
            cachedSnapshotIsBeingTakenMessage = builder.toString();
        }
        return cachedSnapshotIsBeingTakenMessage;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : getDisksList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    disk.getActualSize()));
        }

        return list;
    }

    /////////////////////////////////////////
    /// TaskHandlerCommand implementation ///
    /////////////////////////////////////////

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

    public void preventRollback() {
        throw new NotImplementedException();
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        super.setExecutionContext(executionContext);
    }

    public Guid createTask(AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(asyncTaskCreationInfo, parentCommand,
                entityType, entityIds);
    }

    public Guid createTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(asyncTaskCreationInfo, parentCommand);
    }

    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }
}
