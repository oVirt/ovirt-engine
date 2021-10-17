package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateScratchDiskCommand<T extends AddDiskParameters> extends CommandBase<T> {

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public CreateScratchDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateScratchDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        setVmId(((VmBackupParameters) getParameters().getParentParameters()).getVmBackup().getVmId());
        if (Guid.isNullOrEmpty(getStoragePoolId())) {
            setStoragePoolId(getCluster().getStoragePoolId());
        }
    }

    @Override
    protected void executeCommand() {
        DiskImage disk = (DiskImage) getParameters().getDiskInfo();
        DiskImage image = new DiskImage();
        image.setSize(getImageSize(disk));
        image.setInitialSizeInBytes(getInitialSize(disk));
        image.setVolumeType(VolumeType.Sparse);
        image.setVolumeFormat(VolumeFormat.COW);
        image.setWipeAfterDelete(disk.isWipeAfterDelete());
        image.setStorageIds(disk.getStorageIds());
        image.setContentType(DiskContentType.BACKUP_SCRATCH);
        // Set scratch disk status as ILLEGAL to prevent wrong usage of that disk.
        image.setImageStatus(ImageStatus.ILLEGAL);

        Guid backupId = ((VmBackupParameters) getParameters().getParentParameters()).getVmBackup().getId();
        image.setDiskDescription(String.format("Backup %s scratch disk", backupId));
        image.setDiskAlias(
                String.format("VM %s backup %s scratch disk for %s", getVm().getName(), backupId, disk.getDiskAlias()));
        Date creationDate = new Date();
        image.setCreationDate(creationDate);
        image.setLastModified(creationDate);

        AddDiskParameters diskParameters = new AddDiskParameters(image);
        diskParameters.setStorageDomainId(disk.getStorageIds().get(0));
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setShouldRemainLockedOnSuccesfulExecution(true);
        diskParameters.setSkipDomainCheck(true);

        log.info("Creating scratch disk for disk ID {}", disk.getId());
        ActionReturnValue actionReturnValue = runInternalActionWithTasksContext(ActionType.AddDisk, diskParameters);
        Guid createdDiskId = actionReturnValue.getActionReturnValue();
        setActionReturnValue(createdDiskId);

        setSucceeded(actionReturnValue.getSucceeded());
    }

    private long getImageSize(DiskImage disk) {
        // TODO: For block-based RAW disks, actual size may be bigger than virtual size
        // due to lvm extent size (128m), should be investigated and fixed.
        return disk.getStorageTypes().get(0).isBlockDomain() && disk.getVolumeFormat() == VolumeFormat.RAW
                ? disk.getActualSizeInBytes() : disk.getSize();
    }

    private long getInitialSize(DiskImage disk) {
        // Scratch disk created as COW-Sparse, so the initial size should be:
        //  - File-based scratch disk: initial size should be - '0' (which mapped to None in VDSM),
        //    since initial size isn't supported for file-based storage domains.
        //
        //  - Block-based scratch disk: The size should consider requested scratch disk size percentage
        //                              MaxBackupBlockScratchDiskInitialSizePercents value from the
        //                              config and not less then MinBackupBlockScratchDiskInitialSizeInGB from
        //                              the config (default 4GB).
        //      * Backed-up disk format is RAW: the initial size should be the backed-up disk actual size * percentage.
        //      * Backed-up disk format is COW: the initial size should be measured and padded by 1GB
        //        chunk size * percentage.

        if (!disk.getStorageTypes().get(0).isBlockDomain()) {
            return 0L;
        }

        long maxInitialSize;
        if (disk.getVolumeFormat() == VolumeFormat.RAW) {
            maxInitialSize = disk.getActualSizeInBytes();
        } else {
            long measuredImageSize = measureImageChainSize(disk);
            // Padding the image for data that might be written
            // after we measured the disk and before backup was started.
            measuredImageSize += SizeConverter.BYTES_IN_GB;
            // If the measured padded size is bigger then the image virtual we should not use it.
            maxInitialSize = Math.min(measuredImageSize, disk.getSize());
        }

        return calculateInitialSizeForBlockDisk(maxInitialSize, disk.getSize());
    }

    private long measureImageChainSize(DiskImage disk) {
        // Scratch disk creation takes place only on live backup flow and as part of creating scratch disks
        // parent command which received VmBackupParameters, so it is safe to cast the parent parameters to it.
        Guid hostId = ((VmBackupParameters) getParameters().getParentParameters()).getVmBackup().getHostId();

        MeasureVolumeParameters parameters = new MeasureVolumeParameters(disk.getStoragePoolId(),
                disk.getStorageIds().get(0),
                disk.getId(),
                disk.getImageId(),
                disk.getVolumeFormat().getValue());
        parameters.setParentCommand(getActionType());
        parameters.setEndProcedure(EndProcedure.PARENT_MANAGED);
        parameters.setVdsRunningOn(hostId);
        parameters.setCorrelationId(getCorrelationId());
        ActionReturnValue actionReturnValue =
                runInternalAction(ActionType.MeasureVolume, parameters,
                        ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!actionReturnValue.getSucceeded()) {
            throw new RuntimeException(
                    "Could not measure the initial size of " + disk.getName() + " for scratch disk creation.");
        }

        return actionReturnValue.getActionReturnValue();
    }

    private long calculateInitialSizeForBlockDisk(long maxInitialSize, long diskSize) {
        // Sets the scratch disk size according to the percentage from the backed-up disk size
        // (MaxBackupBlockScratchDiskInitialSizePercents) set in the config.
        // Size cannot be less than MinBackupBlockScratchDiskInitialSizeInGB or the disk virtual size if it is smaller.
        Integer scratchDiskSizePercentage = Config.<Integer> getValue(ConfigValues.MaxBackupBlockScratchDiskInitialSizePercents);
        long minScratchDiskSize = Config.<Integer> getValue(ConfigValues.MinBackupBlockScratchDiskInitialSizeInGB) * SizeConverter.BYTES_IN_GB;
        long initialSizeByPercentage = (scratchDiskSizePercentage * maxInitialSize) / 100;
        long initialSize = Math.max(initialSizeByPercentage, minScratchDiskSize);
        return Math.min(initialSize, diskSize);
    }

    private void endChildCommand(boolean succeeded) {
        if (!getParameters().getImagesParameters().isEmpty()) {
            ActionParametersBase childParams = getParameters().getImagesParameters().get(0);
            childParams.setTaskGroupSuccess(succeeded);
            backend.endAction(childParams.getCommandType(), childParams,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVm().getName());
        addCustomValue("diskId", getParameters().getDiskInfo().getId().toString());
        addCustomValue("backupId",
                ((VmBackupParameters) getParameters().getParentParameters()).getVmBackup().getId().toString());
        switch (getActionState()) {
            case EXECUTE:
                return AuditLogType.VM_BACKUP_SCRATCH_DISK_CREATION_STARTED;
            case END_SUCCESS:
                if (getSucceeded()) {
                    return AuditLogType.VM_BACKUP_SCRATCH_DISK_CREATION_SUCCEEDED;
                }
                return AuditLogType.VM_BACKUP_SCRATCH_DISK_CREATION_FAILED;
            default:
                return AuditLogType.VM_BACKUP_SCRATCH_DISK_CREATION_FAILED;
            }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SCRATCH_DISK);
        addValidationMessage(EngineMessage.VAR__ACTION__BACKUP);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                ActionGroup.CREATE_DISK));
    }

    @Override
    protected void endSuccessfully() {
        endChildCommand(true);
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endWithFailure() {
        endChildCommand(false);
        setSucceeded(true);
    }
}
