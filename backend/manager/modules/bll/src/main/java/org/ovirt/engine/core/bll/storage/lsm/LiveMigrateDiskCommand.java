package org.ovirt.engine.core.bll.storage.lsm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.MoveOrCopyDiskCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneImageGroupVolumesStructureCommandParameters;
import org.ovirt.engine.core.common.action.CopyImageGroupVolumesDataCommandParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters.LiveDiskMigrateStage;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class LiveMigrateDiskCommand<T extends LiveMigrateDiskParameters> extends MoveOrCopyDiskCommand<T>
        implements SerialChildExecutingCommand {

    private Guid sourceQuotaId;
    private Guid sourceDiskProfileId;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ResourceManager resourceManager;
    @Inject
    private ImageDao imageDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private SnapshotDao snapshotDao;

    private Map<Guid, DiskImage> diskImagesMap = new HashMap<>();

    private StorageDomain dstStorageDomain;

    public LiveMigrateDiskCommand(Guid commandId) {
        super(commandId);
    }

    public LiveMigrateDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void init() {
        super.init();
        setStoragePoolId(getVm().getStoragePoolId());
        getParameters().setStoragePoolId(getStoragePoolId());

        getParameters().setVdsId(getVdsId());
        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());

        getParameters().setDestinationImageId(((DiskImage) getDiskImageByDiskId(getParameters().getImageGroupID()))
                .getImageId());
    }

    private Disk getDiskImageByDiskId(Guid diskId) {
        Disk disk = diskDao.get(diskId);
        if (disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            if (!diskImagesMap.containsKey(diskImage.getImageId())) {
                diskImagesMap.put(diskImage.getImageId(), (DiskImage) disk);
            }
        }
        return disk;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        DiskImage diskImage = diskImageDao.get(getParameters().getImageId());
        return Collections.singletonList(new PermissionSubject(diskImage.getId(),
                VdcObjectType.Disk,
                ActionGroup.DISK_LIVE_STORAGE_MIGRATION));
    }

    private CloneImageGroupVolumesStructureCommandParameters buildCloneImageGroupVolumesStructureCommandParams() {
        CloneImageGroupVolumesStructureCommandParameters p =
                new CloneImageGroupVolumesStructureCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId(),
                        getParameters().getDestDomainId(),
                        getImageGroupId(),
                        getActionType(),
                        getParameters());
        p.setDestImageGroupId(getImageGroupId());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        p.setJobWeight(Job.MAX_WEIGHT);
        p.setLiveMigration(true);

        return p;
    }

    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void executeCommand() {
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(getImageGroupIds(),
                ImageStatus.LOCKED,
                ImageStatus.OK,
                getCompensationContext());
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.CreateSnapshotForVm,
                getCreateSnapshotParameters(),
                ExecutionHandler.createInternalJobContext(getContext(), getLock()));
        getParameters().setAutoGeneratedSnapshotId(actionReturnValue.getActionReturnValue());
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
        setSucceeded(true);
    }

    protected CreateSnapshotForVmParameters getCreateSnapshotParameters() {
        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(getParameters().getVmId(),
            getDiskAlias() + " " + StorageConstants.LSM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION, false);

        params.setParentCommand(ActionType.LiveMigrateDisk);
        params.setSnapshotType(Snapshot.SnapshotType.REGULAR);
        params.setParentParameters(getParameters());
        params.setImagesParameters(getParameters().getImagesParameters());
        params.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
        params.setDiskIds(getImageGroupIds());
        params.setDiskIdsToIgnoreInChecks(getImageGroupIds());
        params.setNeedsLocking(false);
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        params.setDiskImagesMap(createDiskImagesMap());

        return params;
    }

    private Set<Guid> getImageGroupIds() {
        return Collections.singleton(getImageGroupId());
    }

    private Map<Guid, DiskImage> createDiskImagesMap() {
        if (FeatureSupported.isReplicateExtendSupported(getCluster().getCompatibilityVersion())) {
            StorageDomainStatic sourceDomain = storageDomainStaticDao.get(getParameters().getSourceDomainId());
            if (sourceDomain.getStorageType().isBlockDomain()) {
                DiskImage disk = getDiskImage();
                DiskImage diskParams = new DiskImage();

                diskParams.setInitialSizeInBytes(ImagesHandler.computeImageInitialSizeInBytes(disk.getImage()));
                Map<Guid, DiskImage> diskImagesMap = new HashMap<>();
                diskImagesMap.put(disk.getId(), diskParams);
                return diskImagesMap;
            }
        }
        return new HashMap<>();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.CREATE_SNAPSHOT) {
            runInternalAction(ActionType.CloneImageGroupVolumesStructure,
                    buildCloneImageGroupVolumesStructureCommandParams(),
                    ExecutionHandler.createInternalJobContext(createStepsContext(StepEnum.CLONE_IMAGE_STRUCTURE,
                            Collections.emptyMap())));
            updateStage(LiveDiskMigrateStage.CLONE_IMAGE_STRUCTURE);
            return true;
        }

        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.CLONE_IMAGE_STRUCTURE) {
            updateStage(LiveDiskMigrateStage.VM_REPLICATE_DISK_START);
            replicateDiskStart();
            updateStage(LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_START);
            syncImageData();
            updateStage(LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_END);
            return true;
        }

        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_END ||
                getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.VM_REPLICATE_DISK_FINISH) {
            updateStage(LiveDiskMigrateStage.VM_REPLICATE_DISK_FINISH);

            // in case live migration is yet to complete (VM under heavy I/O load), we shall retry to pivot
            // in the next polling cycle
            if (!completeLiveMigration()) {
                return true;
            }

            updateStage(LiveDiskMigrateStage.SOURCE_IMAGE_DELETION);
            removeImage(getParameters().getSourceDomainId(),
                    getParameters().getImageGroupID(),
                    getParameters().getDestinationImageId(),
                    AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_SRC_IMAGE);
            updateStage(LiveDiskMigrateStage.LIVE_MIGRATE_DISK_EXEC_COMPLETED);
            return true;
        }

        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.LIVE_MIGRATE_DISK_EXEC_COMPLETED) {
            // This lock is required to prevent CreateSnapshotForVmCommand from
            // running at the same time as RemoveSnapshotCommand
            //
            // This lock was previously acquired by the parent command, but it was released
            // at the end of CreateSnapshotForVm command called from the executeCommand() method.
            // Here the lock is acquired again and will be released when this command (LiveMigrateDisk)
            // finishes.
            EngineLock removeSnapshotLock = getEngineLockForSnapshotRemove();
            if (!lockManager.acquireLock(removeSnapshotLock).isAcquired()) {
                log.info("Failed to acquire VM lock, will retry on the next polling cycle");
                return true;
            }

            updateStage(LiveDiskMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_START);
            CommandContext commandContext =
                    ExecutionHandler.createInternalJobContext(createStepsContext(StepEnum.MERGE_SNAPSHOTS,
                            getMergeSnapshotsJobMessageProperties()));
            removeAutogeneratedSnapshot(commandContext, getActionType(), getParameters());
            updateStage(LiveDiskMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_END);

            return true;
        }

        return false;
    }

    private void removeImage(Guid storageDomainId, Guid imageGroupId, Guid imageId, AuditLogType failureAuditLog) {
        RemoveImageParameters removeImageParams =
                new RemoveImageParameters(imageId);
        removeImageParams.setStorageDomainId(storageDomainId);
        removeImageParams.setParentCommand(ActionType.RemoveImage);
        removeImageParams.setDbOperationScope(ImageDbOperationScope.NONE);
        removeImageParams.setShouldLockImage(false);
        ActionReturnValue returnValue = runInternalAction(
                ActionType.RemoveImage,
                removeImageParams,
                cloneContextAndDetachFromParent());
        if (returnValue.getSucceeded()) {
            startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
        } else {
            addCustomValue("DiskAlias", baseDiskDao.get(imageGroupId).getDiskAlias());
            addCustomValue("StorageDomainName", storageDomainStaticDao.get(storageDomainId).getName());
            addCustomValue("UserName", getUserName());
            auditLogDirector.log(this, failureAuditLog);
        }
    }

    private void removeAutogeneratedSnapshot(CommandContext commandContext, ActionType actionType, LiveMigrateDiskParameters parameters) {
        RemoveSnapshotParameters removeSnapshotParameters = new RemoveSnapshotParameters(getParameters().getAutoGeneratedSnapshotId(),
                getVmId());
        removeSnapshotParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        if (actionType != null) {
            removeSnapshotParameters.setParentCommand(actionType);
        }
        removeSnapshotParameters.setParentParameters(parameters);
        removeSnapshotParameters.setNeedsLocking(false);


        runInternalAction(ActionType.RemoveSnapshot,
                removeSnapshotParameters,
                commandContext);
    }

    private CommandContext createStepsContext(StepEnum step, Map<String, String> jobMessageProperties) {
        Step addedStep = executionHandler.addSubStep(getExecutionContext(),
                getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                step,
                ExecutionMessageDirector.resolveStepMessage(step, jobMessageProperties));
        ExecutionContext ctx = new ExecutionContext();
        ctx.setStep(addedStep);
        ctx.setMonitored(true);
        return ExecutionHandler.createDefaultContextForTasks(getContext(), null)
                .withExecutionContext(ctx);
    }

    private void unlockDisk() {
        imageDao.updateStatusOfImagesByImageGroupId(getParameters().getImageGroupID(), ImageStatus.OK);
    }

    private void releaseSnapshotLock() {
        EngineLock removeSnapshotLock = createEngineLockForSnapshotRemove();
        lockManager.releaseLock(removeSnapshotLock);
    }

    private EngineLock createEngineLockForSnapshotRemove() {
        return new EngineLock(
                getExclusiveLocksForSnapshotRemove(),
                getSharedLocksForSnapshotRemove());
    }

    private EngineLock getEngineLockForSnapshotRemove() {
        EngineLock lock = getLock();
        if (lock != null && !lock.getExclusiveLocks().isEmpty()) {
            // In the generic scenario, use the lock created by the MigrateDiskCommand
            return lock;
        }
        // This happens when the engine was restarted during active live disk migration. Use special lock to clean up
        // temporary VM snapshot created by the migration
        return createEngineLockForSnapshotRemove();
    }

    private boolean isConsiderSuccessful() {
        return getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.AUTO_GENERATED_SNAPSHOT_REMOVE_END;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        updateImagesInfo();
        unlockDisk();
        releaseSnapshotLock();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // if we failed to remove the source image, we should add an audit log and consider the operation as successful.
        if (isConsiderSuccessful()) {
            auditLog(this, AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_SRC_IMAGE);
            this.endSuccessfully();
            return;
        }

        // If live migration of the disk was finished without errors but live merge
        // failed we will consider this operation as successful.
        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.LIVE_MIGRATE_DISK_EXEC_COMPLETED) {
            auditLog(this, AuditLogType.USER_MOVED_DISK_FINISHED_WITH_LEFTOVERS);
            this.endSuccessfully();
            return;
        }

        super.endWithFailure();
        cleanupDestDiskAfterFailure();

        // Handle snapshot removal
        log.info("Attempting to remove the auto-generated snapshot");
        LiveMigrateDiskParameters params = getParameters();
        params.setCommandId(null);
        removeAutogeneratedSnapshot(cloneContextAndDetachFromParent(), null, params);

        unlockDisk();
        setSucceeded(true);
    }

    private boolean completeLiveMigration() {
        // Update the DB before sending the command (perform rollback on failure)
        moveDiskInDB(getParameters().getSourceDomainId(),
                getParameters().getDestDomainId(),
                getParameters().getQuotaId(),
                getParameters().getDiskProfileId());

        try {
            replicateDiskFinish(getParameters().getSourceDomainId(), getParameters().getDestDomainId());
        } catch (Exception e) {
            if (e instanceof EngineException &&
                    EngineError.unavail.equals(((EngineException) e).getErrorCode())) {
                log.warn("Replication not finished yet, will retry in next polling cycle");
                return false;
            }

            moveDiskInDB(getParameters().getDestDomainId(),
                    getParameters().getSourceDomainId(),
                    sourceQuotaId,
                    sourceDiskProfileId);
            log.error("Failed VmReplicateDiskFinish (Disk '{}', VM '{}')",
                    getParameters().getImageGroupID(),
                    getParameters().getVmId());
            throw e;
        }

        return true;
    }

    private void replicateDiskFinish(Guid srcDomain, Guid dstDomain) {
        VmReplicateDiskParameters migrationFinishParams = new VmReplicateDiskParameters(getParameters().getVdsId(),
                getParameters().getVmId(),
                getParameters().getStoragePoolId(),
                srcDomain,
                dstDomain,
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId(),
                null);

        VDSReturnValue ret = resourceManager.runVdsCommand(
                VDSCommandType.VmReplicateDiskFinish, migrationFinishParams);

        if (!ret.getSucceeded()) {
            throw new EngineException(ret.getVdsError().getCode(), ret.getVdsError().getMessage());
        }
    }

    private boolean isMoveDiskInDbSucceeded(Guid destDomainId) {
        Guid destinationImageId = getParameters().getDestinationImageId();
        DiskImage diskImage = diskImageDao.get(destinationImageId);
        return diskImage != null && destDomainId.equals(diskImage.getStorageIds().get(0));
    }

    private void moveDiskInDB(final Guid sourceDomainId,
            final Guid destDomainId,
            final Guid destQuota,
            final Guid destDiskProfile) {
        if (isMoveDiskInDbSucceeded(destDomainId)) {
            return;
        }

        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    for (DiskImage di : diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID())) {
                        imageStorageDomainMapDao.remove(new ImageStorageDomainMapId(di.getImageId(),
                                sourceDomainId));
                        imageStorageDomainMapDao.save(new ImageStorageDomainMap(di.getImageId(),
                                destDomainId,
                                destQuota,
                                destDiskProfile));
                        // since moveDiskInDB can be called to 'rollback' the entity in case of
                        // an exception, we store locally the old quota and disk profile id.
                        if (sourceQuotaId == null) {
                            sourceQuotaId = di.getQuotaId();
                        }

                        if (sourceDiskProfileId == null) {
                            sourceDiskProfileId = di.getDiskProfileId();
                        }
                    }
                    return null;
                });
    }

    private void updateImagesInfo() {
        for (DiskImage image : diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID())) {
            VDSReturnValue ret = runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(getParameters().getStoragePoolId(),
                            getParameters().getDestDomainId(),
                            getParameters().getImageGroupID(),
                            image.getImageId()));
            DiskImage imageFromIRS = (DiskImage) ret.getReturnValue();

            if (imageFromIRS != null) {
                // If an image is RAW/sparse and copied to a block SD it will become COW,
                // use the information fetched from the storage to ensure it's the format change
                // is persisted and visible in the engine.
                image.setVolumeFormat(imageFromIRS.getVolumeFormat());

                setQcowCompatForSnapshot(image, imageFromIRS);
                DiskImageDynamic diskImageDynamic = diskImageDynamicDao.get(image.getImageId());

                // Update image's actual size in DB
                if (diskImageDynamic != null) {
                    diskImageDynamic.setActualSize(imageFromIRS.getActualSizeInBytes());
                    diskImageDynamicDao.update(diskImageDynamic);
                }
            }
        }
    }

    private void syncImageData() {
        Guid vdsId = vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId());
        CopyImageGroupVolumesDataCommandParameters parameters =
                new CopyImageGroupVolumesDataCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId(),
                        getParameters().getImageGroupID(),
                        getParameters().getDestDomainId(),
                        getActionType(),
                        getParameters());

        parameters.setVdsRunningOn(vdsId);
        parameters.setVdsId(vdsId);
        parameters.setJobWeight(Job.MAX_WEIGHT);
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);

        // This would prevent prepare/teardown of the copy_data command as it is not needed
        // for images used by a running VM
        if (vdsId.equals(getVm().getRunOnVds())) {
            parameters.setLive(true);
        }

        runInternalAction(ActionType.CopyImageGroupVolumesData, parameters, createStepsContext(StepEnum.SYNC_IMAGE_DATA,
                Collections.emptyMap()));
    }

    private void replicateDiskStart() {
        if (Guid.Empty.equals(getParameters().getVdsId())) {
            throw new EngineException(EngineError.down,
                    "VM " + getParameters().getVmId() + " is not running on any VDS");
        }

        StorageType destType = getDstStorageDomain().getStorageStaticData().getStorageType();
        Optional<String> diskType = vmInfoBuildUtils.getNetworkDiskType(getVm(), destType);

        // Start disk migration
        VmReplicateDiskParameters migrationStartParams = new VmReplicateDiskParameters(getParameters().getVdsId(),
                getParameters().getVmId(),
                getParameters().getStoragePoolId(),
                getParameters().getSourceDomainId(),
                getParameters().getDestDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId(),
                diskType.orElse(null));
        if (FeatureSupported.isReplicateExtendSupported(getCluster().getCompatibilityVersion())) {
            migrationStartParams.setNeedExtend(false);
        }

        VDSReturnValue ret = resourceManager.runVdsCommand(VDSCommandType.VmReplicateDiskStart, migrationStartParams);
        if (!ret.getSucceeded()) {
            log.error("Failed VmReplicateDiskStart (Disk '{}' , VM '{}')",
                    getParameters().getImageGroupID(),
                    getParameters().getVmId());
            throw new EngineException(ret.getVdsError().getCode(), ret.getVdsError().getMessage());
        }
    }

    private void updateStage(LiveDiskMigrateStage stage) {
        getParameters().setLiveDiskMigrateStage(stage);
        persistCommand(getParameters().getParentCommand(), getCallback() != null);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected boolean checkCanBeMoveInVm() {
        return validate(createDiskValidator(getDiskImage()).isDiskPluggedToAnyNonDownVm(true));
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm == null) {
            vm = vmDao.getVmsListForDisk(getImageGroupId(), false).get(0);
            setVm(vm);
            setVmId(vm.getId());
        }
        return vm;
    }

    @Override
    public Guid getVdsId() {
        return getVm().getRunOnVds() != null ? getVm().getRunOnVds() : Guid.Empty;
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (!getVm().isRunningAndQualifyForDisksMigration()) {
            return failValidation(EngineMessage.CANNOT_LIVE_MIGRATE_VM_SHOULD_BE_IN_PAUSED_OR_UP_STATUS);
        }

        if (!validate(new StorageDomainValidator(getDstStorageDomain()).isNotBackupDomain())
                || !validateDestDomainsSpaceRequirements()) {
            return false;
        }

        getReturnValue().setValid(isDiskNotShareable(getParameters().getImageId())
                    && isDiskSnapshotNotPluggedToOtherVmsThatAreNotDown(getParameters().getImageId()));

        if (!getReturnValue().isValid()) {
            return false;
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        return validateCreateSnapshotForVmCommand();
    }

    protected boolean validateCreateSnapshotForVmCommand() {
        ActionReturnValue returnValue = CommandHelper.validate(ActionType.CreateSnapshotForVm,
                getCreateSnapshotParameters(), getContext().clone());
        if (!returnValue.isValid()) {
            getReturnValue().setValidationMessages(returnValue.getValidationMessages());
            return false;
        }
        return true;
    }

    private StorageDomain getDstStorageDomain() {
        if (dstStorageDomain == null) {
            dstStorageDomain = storageDomainDao.getForStoragePool(getParameters().getDestDomainId(),
                    getStoragePoolId());
        }
        return dstStorageDomain;
    }

    protected boolean validateDestDomainsSpaceRequirements() {
        DiskImage diskImage = getDiskImageByImageId(getParameters().getImageId());
        List<DiskImage> allImageSnapshots = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());
        diskImage.getSnapshots().addAll(allImageSnapshots);

        StorageDomainValidator storageDomainValidator = createStorageDomainValidator(getDstStorageDomain());
        if (!validate(storageDomainValidator.hasSpaceForClonedDisks(Collections.singleton(diskImage)))) {
            return false;
        }

        return true;
    }

    private DiskImage getDiskImageByImageId(Guid imageId) {
        if (diskImagesMap.containsKey(imageId)) {
            return diskImagesMap.get(imageId);
        }

        DiskImage diskImage = diskImageDao.get(imageId);
        diskImagesMap.put(imageId, diskImage);

        return diskImage;
    }

    private boolean isDiskNotShareable(Guid imageId) {
        DiskImage diskImage = getDiskImageByImageId(imageId);

        if (diskImage.isShareable()) {
            addValidationMessageVariable("diskAliases", diskImage.getDiskAlias());
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED);
        }

        return true;
    }

    protected boolean isDiskSnapshotNotPluggedToOtherVmsThatAreNotDown(Guid imageId) {
        return validate(createDiskValidator(getDiskImageByImageId(imageId)).isDiskPluggedToAnyNonDownVm(true));
    }

    protected StorageDomainValidator createStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain) {
            @Override
            protected double getTotalSizeForClonedDisk(DiskImage diskImage) {
                double basicSize = super.getTotalSizeForClonedDisk(diskImage);
                // Add additional snapshot overhead (relevant only for Live Storage flow, cluster version 4.7 or above).
                if (FeatureSupported.isReplicateExtendSupported(getCluster().getCompatibilityVersion())) {
                    basicSize += ImagesHandler.computeImageInitialSizeInBytes(diskImage.getImage());
                }
                return basicSize;
            }
        };
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getActionState() == CommandActionState.EXECUTE) {
            if (!getParameters().getTaskGroupSuccess()) {
                return AuditLogType.USER_MOVED_DISK_FINISHED_FAILURE;
            }
            if (getSucceeded()) {
                return AuditLogType.USER_MOVED_DISK;
            }
        } else if (getActionState() == CommandActionState.END_SUCCESS || isConsiderSuccessful()) {
            return AuditLogType.USER_MOVED_DISK_FINISHED_SUCCESS;
        } else {
            return AuditLogType.USER_MOVED_DISK_FINISHED_FAILURE;
        }

        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    private Map<String, Pair<String, String>> getExclusiveLocksForSnapshotRemove() {
        return Collections.singletonMap(getParameters().getImageGroupID().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                        getDiskIsBeingMigratedMessage(getDiskImageByDiskId(getParameters().getImageGroupID()))));
    }

    private Map<String, Pair<String, String>> getSharedLocksForSnapshotRemove() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    private String getDiskIsBeingMigratedMessage(Disk disk) {
        return EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED.name()
                + String.format("$DiskName %1$s", disk != null ? disk.getDiskAlias() : "");
    }

    private void cleanupDestDiskAfterFailure() {
        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.CREATE_SNAPSHOT ||
                getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.SOURCE_IMAGE_DELETION) {
            return;
        }

        if (getParameters().getLiveDiskMigrateStage() != LiveDiskMigrateStage.CLONE_IMAGE_STRUCTURE) {
            if (Guid.Empty.equals(getParameters().getVdsId())) {
                log.error("Failed during live storage migration of disk '{}' of vm '{}', as the vm is not running" +
                                " on any host not attempting to end the replication before the destination disk deletion",
                        getParameters().getImageGroupID(), getParameters().getVmId());
            } else {
                log.error("Failed during live storage migration of disk '{}' of vm '{}', attempting to end " +
                        "replication before deleting the destination disk",
                        getParameters().getImageGroupID(), getParameters().getVmId());
                try {
                    replicateDiskFinish(getParameters().getSourceDomainId(), getParameters().getSourceDomainId());
                } catch (Exception e) {
                    if (e instanceof EngineException &&
                            EngineError.ReplicationNotInProgress.equals(((EngineException) e).getErrorCode())) {
                        log.warn("Replication is not in progress, proceeding with removing the destination disk");
                    } else {
                        log.error("Replication end of disk '{}' in vm '{}' back to the source failed, skipping deletion of " +
                                "the destination disk", getParameters().getImageGroupID(), getParameters().getVmId());
                        return;
                    }
                }
            }
        }

        log.error("Attempting to delete the destination of disk '{}' of vm '{}'",
                getParameters().getImageGroupID(), getParameters().getVmId());
        removeImage(getParameters().getDestDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId(),
                AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_DST_IMAGE);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(), getVmName());
        }
        return jobProperties;
    }

    private Map<String, String> getMergeSnapshotsJobMessageProperties() {
        DiskImage diskImage = diskImageDao.getSnapshotById(getParameters().getImageId());
        DiskImage destDiskImage = diskImageDao.getSnapshotById(getParameters().getDestinationImageId());

        Map<String, String> jobMessageProperties = new HashMap<>();
        jobMessageProperties.put(VdcObjectType.Disk.name().toLowerCase(), diskImage.getDiskAlias());
        jobMessageProperties.put("sourcesnapshot",
                Optional.ofNullable(snapshotDao.get(diskImage.getVmSnapshotId()).getDescription()).orElse(""));
        jobMessageProperties.put("destinationsnapshot",
                Optional.ofNullable(snapshotDao.get(destDiskImage.getVmSnapshotId()).getDescription()).orElse(""));

        return jobMessageProperties;
    }

}
