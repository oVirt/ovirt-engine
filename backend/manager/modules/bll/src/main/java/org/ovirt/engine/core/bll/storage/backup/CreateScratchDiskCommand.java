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
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateScratchDiskCommand<T extends AddDiskParameters> extends CommandBase<T> {
    @Inject
    protected ImagesHandler imagesHandler;
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
        image.setSize(disk.getSize());
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

    private Long getInitialSize(DiskImage disk) {
        // For block-based COW disks we should fetch the up-to-date
        // LV size from the host for setting the initial size.
        if (disk.getStorageTypes().get(0).isBlockDomain() && disk.getVolumeFormat() == VolumeFormat.COW) {
            log.info("Getting volume info for image '{}/{}'", disk.getId(), disk.getImageId());
            try {
                DiskImage imageFromVdsm = imagesHandler.getVolumeInfoFromVdsm(disk.getStoragePoolId(),
                        disk.getStorageIds().get(0),
                        disk.getId(),
                        disk.getImageId());
                return imageFromVdsm.getApparentSizeInBytes();
            } catch (Exception e) {
                log.error("Failed to get volume info", e);
                throw e;
            }
        }
        // Initial size should be '0'.
        return 0L;
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
