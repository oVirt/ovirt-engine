package org.ovirt.engine.core.bll.storage.lsm;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.MoveOrCopyDiskCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.CreateImagePlaceholderCommandParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters.LiveDiskMigrateStage;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SyncImageGroupDataCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class LiveMigrateDiskCommand<T extends LiveMigrateDiskParameters> extends MoveOrCopyDiskCommand<T>implements SerialChildExecutingCommand {

    private Guid sourceQuotaId;
    private Guid sourceDiskProfileId;

    public LiveMigrateDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);

        setStoragePoolId(getVm().getStoragePoolId());
        getParameters().setStoragePoolId(getStoragePoolId());

        getParameters().setVdsId(getVdsId());
        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());
    }

    private CreateImagePlaceholderCommandParameters buildCreateImagePlacerholderParams() {
        CreateImagePlaceholderCommandParameters p = new CreateImagePlaceholderCommandParameters(
                getParameters().getStoragePoolId(),
                getParameters().getImageGroupID(),
                getParameters().getSourceStorageDomainId(),
                getParameters().getTargetStorageDomainId());
        p.setParentCommand(getActionType());
        p.setParentParameters(getParameters());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return p;
    }

    public CommandCallback getCallback() {
        return new SerialChildCommandsExecutionCallback();
    }

    @Override
    protected void executeCommand() {
        runInternalAction(VdcActionType.CreateImagePlaceholder,
                buildCreateImagePlacerholderParams(), createStepsContext(StepEnum.CLONE_IMAGE_STRUCTURE));
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.IMAGE_PLACEHOLDER_CREATION) {
            updateStage(LiveDiskMigrateStage.VM_REPLICATE_DISK_START);
            replicateDiskStart();
            updateStage(LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_START);
            syncImageData();
            updateStage(LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_END);
            return true;
        }

        if (getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.IMAGE_DATA_SYNC_EXEC_END) {
            updateStage(LiveDiskMigrateStage.VM_REPLICATE_DISK_FINISH);
            completeLiveMigration();
            updateStage(LiveDiskMigrateStage.SOURCE_IMAGE_DELETION);
            LiveStorageMigrationHelper.removeImage(this, getParameters().getSourceStorageDomainId(), getParameters()
                    .getImageGroupID(), getParameters().getDestinationImageId(), AuditLogType
                    .USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_SRC_IMAGE);
            return false;
        }

        return false;
    }


    private CommandContext createStepsContext(StepEnum step) {
        Step addedStep = ExecutionHandler.addSubStep(getExecutionContext(),
                getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                step,
                ExecutionMessageDirector.resolveStepMessage(step, Collections.emptyMap()));
        ExecutionContext ctx = new ExecutionContext();
        ctx.setStep(addedStep);
        ctx.setMonitored(true);
        CommandContext commandCtx = ExecutionHandler.createDefaultContextForTasks(getContext(), null)
                .withExecutionContext(ctx);
        return commandCtx;
    }

    private void unlockDisk() {
        ImagesHandler.updateAllDiskImageSnapshotsStatus(
                getParameters().getImageGroupID(), ImageStatus.OK);
    }

    private boolean isConsiderSuccessful() {
        return getParameters().getLiveDiskMigrateStage() == LiveDiskMigrateStage.SOURCE_IMAGE_DELETION;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        updateImagesInfo();
        unlockDisk();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // if we failed to removed the source image, we should add an audit log and consider the
        // operation as successful.
        if (isConsiderSuccessful()) {
            auditLog(this, AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_SRC_IMAGE);
            this.endSuccessfully();
            return;
        }
        super.endWithFailure();
        handleDestDisk();
        unlockDisk();
        setSucceeded(true);
    }

    private void completeLiveMigration() {
        // Update the DB before sending the command (perform rollback on failure)
        moveDiskInDB(getParameters().getSourceStorageDomainId(),
                getParameters().getTargetStorageDomainId(),
                getParameters().getQuotaId(),
                getParameters().getDiskProfileId());

        try {
            replicateDiskFinish(getParameters().getSourceStorageDomainId(),
                    getParameters().getTargetStorageDomainId());
        } catch (Exception e) {
            moveDiskInDB(getParameters().getTargetStorageDomainId(),
                    getParameters().getSourceStorageDomainId(),
                    sourceQuotaId,
                    sourceDiskProfileId);
            log.error("Failed VmReplicateDiskFinish (Disk '{}', VM '{}')",
                    getParameters().getImageGroupID(),
                    getParameters().getVmId());
            throw e;
        }
    }

    private void replicateDiskFinish(Guid srcDomain, Guid dstDomain) {
        VmReplicateDiskParameters migrationStartParams = new VmReplicateDiskParameters(getParameters().getVdsId(),
                getParameters().getVmId(),
                getParameters().getStoragePoolId(),
                srcDomain,
                dstDomain,
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId());

        VDSReturnValue ret = ResourceManager.getInstance().runVdsCommand(
                VDSCommandType.VmReplicateDiskFinish, migrationStartParams);

        if (!ret.getSucceeded()) {
            throw new EngineException(ret.getVdsError().getCode(), ret.getVdsError().getMessage());
        }
    }

    private boolean isMoveDiskInDbSucceded(Guid targetStorageDomainId) {
        Guid destinationImageId = getParameters().getDestinationImageId();
        DiskImage diskImage = getDiskImageDao().get(destinationImageId);
        return diskImage != null && targetStorageDomainId.equals(diskImage.getStorageIds().get(0));
    }

    private void moveDiskInDB(final Guid sourceStorageDomainId,
            final Guid targetStorageDomainId,
            final Guid targetQuota,
            final Guid targetDiskProfile) {
        if (isMoveDiskInDbSucceded(targetStorageDomainId)) {
            return;
        }

        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @SuppressWarnings("synthetic-access")
                    @Override
                    public Object runInTransaction() {
                        for (DiskImage di : getDiskImageDao()
                                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID())) {
                            getImageStorageDomainMapDao().remove(new ImageStorageDomainMapId(di.getImageId(),
                                    sourceStorageDomainId));
                            getImageStorageDomainMapDao().save(new ImageStorageDomainMap(di.getImageId(),
                                    targetStorageDomainId,
                                    targetQuota,
                                    targetDiskProfile));
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
                    }
                });
    }

    private void updateImagesInfo() {
        for (DiskImage image : getDiskImageDao().getAllSnapshotsForImageGroup(getParameters().getImageGroupID())) {
            VDSReturnValue ret = runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(getParameters().getStoragePoolId(),
                            getParameters().getTargetStorageDomainId(),
                            getParameters().getImageGroupID(),
                            image.getImageId()));

            DiskImage imageFromIRS = (DiskImage) ret.getReturnValue();
            DiskImageDynamic diskImageDynamic = getDiskImageDynamicDao().get(image.getImageId());

            // Update image's actual size in DB
            if (imageFromIRS != null && diskImageDynamic != null) {
                diskImageDynamic.setActualSize(imageFromIRS.getActualSizeInBytes());
                getDiskImageDynamicDao().update(diskImageDynamic);
            }
        }
    }

    private void syncImageData() {
        SyncImageGroupDataCommandParameters parameters =
                new SyncImageGroupDataCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getImageGroupID(),
                        getParameters().getSourceStorageDomainId(),
                        getParameters().getTargetStorageDomainId());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        runInternalAction(VdcActionType.SyncImageGroupData, parameters, createStepsContext(StepEnum.SYNC_IMAGE_DATA));
    }

    private void replicateDiskStart() {
        if (Guid.Empty.equals(getParameters().getVdsId())) {
            throw new EngineException(EngineError.down,
                    "VM " + getParameters().getVmId() + " is not running on any VDS");
        }

        // Start disk migration
        VmReplicateDiskParameters migrationStartParams = new VmReplicateDiskParameters(getParameters().getVdsId(),
                getParameters().getVmId(),
                getParameters().getStoragePoolId(),
                getParameters().getSourceStorageDomainId(),
                getParameters().getTargetStorageDomainId(),
                getParameters().getImageGroupID(),
                getParameters().getDestinationImageId());
        VDSReturnValue ret =
                ResourceManager.getInstance().runVdsCommand(VDSCommandType.VmReplicateDiskStart, migrationStartParams);

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
        return validate(createDiskValidator(getDiskImage()).isDiskPluggedToVmsThatAreNotDown(true,
                getVmsWithVmDeviceInfoForDiskId()));
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm == null) {
            vm = getVmDao().getVmsListForDisk(getImageGroupId(), false).get(0);
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
        boolean validate = super.validate();

        if (!validate) {
            auditLogDirector.log(this, AuditLogType.USER_MOVED_DISK_FINISHED_FAILURE);
        }

        return validate;
    }

    @Override
    protected boolean isImageNotLocked() {
        // During LSM the disks are being locked prior to the snapshot phase
        // therefore returning true here.
        return true;
    }

    protected DiskValidator createDiskValidator(DiskImage disk) {
        return new DiskValidator(disk);
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
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    private void handleDestDisk() {
        if (getParameters().getLiveDiskMigrateStage() != LiveDiskMigrateStage.IMAGE_PLACEHOLDER_CREATION &&
                getParameters().getLiveDiskMigrateStage() != LiveDiskMigrateStage.SOURCE_IMAGE_DELETION) {
            if (Guid.Empty.equals(getParameters().getVdsId())) {
                log.error("Failed during live storage migration of disk '{}' of vm '{}', as the vm is not running" +
                                " on any host not attempting to end the replication before the target disk deletion",
                        getParameters().getImageGroupID(), getParameters().getVmId());
            } else {
                log.error("Failed during live storage migration of disk '{}' of vm '{}', attempting to end " +
                        "replication before deleting the target disk",
                        getParameters().getImageGroupID(), getParameters().getVmId());
                try {
                    replicateDiskFinish(getParameters().getSourceStorageDomainId(),
                            getParameters().getSourceStorageDomainId());
                } catch (Exception e) {
                    log.error("Replication end of disk '{}' in vm '{}' back to the source failed, skipping deletion of " +
                            "the target disk", getParameters().getImageGroupID(), getParameters().getVmId());
                    return;
                }
            }

            log.error("Attempting to delete the target of disk '{}' of vm '{}'",
                    getParameters().getImageGroupID(), getParameters().getVmId());
            LiveStorageMigrationHelper.removeImage(this, getParameters().getTargetStorageDomainId(), getParameters()
                    .getImageGroupID(), getParameters().getDestinationImageId(), AuditLogType
                    .USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_DST_IMAGE);
        }
    }
}
