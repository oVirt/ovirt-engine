package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.action.VmCheckpointParameters;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.VmCheckpointState;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class DeleteVmCheckpointCommand<T extends VmCheckpointParameters> extends VmCommand<T> {

    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private VmBackupDao vmBackupDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    private VmCheckpoint vmCheckpoint;

    public DeleteVmCheckpointCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        vmCheckpoint = vmCheckpointDao.get(getParameters().getVmCheckpoint().getId());
        if (vmCheckpoint != null) {
            vmCheckpoint.setDisks(vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId()));
            setVmId(vmCheckpoint.getVmId());
        }
        setVdsId(getVm().getRunOnVds());
    }

    @Override
    protected boolean validate() {
        if (vmCheckpoint == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CHECKPOINT_NOT_EXIST,
                    String.format("$checkpointId %s", getParameters().getVmCheckpoint().getId()));
        }

        if (isVmDuringBackup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
        }

        DiskExistenceValidator diskExistenceValidator = createDiskExistenceValidator(getDiskIds());
        if (!validate(diskExistenceValidator.disksNotExist())) {
            return false;
        }

        DiskImagesValidator diskImagesValidator = createDiskImagesValidator(vmCheckpoint.getDisks());
        return validate(diskImagesValidator.diskImagesNotLocked());
    }

    @Override
    protected void executeCommand() {
        if (!isLiveDeleteCheckpoint()) {
            setSucceeded(coldDeleteVmCheckpoint());
            return;
        }

        if (!redefineVmCheckpoint()) {
            return;
        }
        setSucceeded(liveDeleteVmCheckpoint());
    }

    private boolean liveDeleteVmCheckpoint() {
        log.info("Live deleting VM checkpoint '{}'", vmCheckpoint.getId());
        try {
            VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.DeleteVmCheckpoints,
                    new VmCheckpointsVDSParameters(getVdsId(),
                            getParameters().getVmId(),
                            List.of(getParameters().getVmCheckpoint())));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            updateCheckpointsChainInDb();
            return true;
        } catch (EngineException e) {
            log.error("Failed to execute VM.delete_checkpoints: {}", e.getMessage());
            return false;
        }
    }

    private boolean coldDeleteVmCheckpoint() {
        log.info("Cold deleting VM checkpoint '{}'", vmCheckpoint.getId());
        Guid vdsId = vdsCommandsHelper.getHostForExecution(getStoragePoolId(), VDS::isColdBackupEnabled);
        if (Guid.isNullOrEmpty(vdsId)) {
            log.error("Failed to find a host to run remove bitmap operation for VM '{}'", getVmName());
            return false;
        }

        log.info("Invalidating VM '{}' checkpoint '{}'.", getVmName(), vmCheckpoint.getId());
        TransactionSupport.executeInNewTransaction(() -> {
            vmCheckpoint.setState(VmCheckpointState.INVALID);
            vmCheckpointDao.update(vmCheckpoint);
            return null;
        });

        // All the bitmaps should be removed from all the disks volumes that were part of the backup.
        boolean bitmapsRemoved = true;
        List<DiskImage> checkpointDisks = vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId());
        for (DiskImage diskImage : checkpointDisks) {
            bitmapsRemoved &= removeBitmapFromDisk(diskImage.getImageId(), vdsId);
        }
        // Removing the checkpoint from the Engine database if all bitmaps were removed.
        if (bitmapsRemoved) {
            updateCheckpointsChainInDb();
        }
        return bitmapsRemoved;
    }

    private boolean removeBitmapFromDisk(Guid imageId, Guid vdsId) {
        boolean bitmapsRemoved = true;
        List<DiskImage> diskImages = diskImageDao.getAllSnapshotsForLeaf(imageId);

        // Remove the bitmap from all the disk snapshots.
        for (DiskImage image : diskImages) {
            VdsmImageLocationInfo locationInfo = new VdsmImageLocationInfo(
                    image.getStorageIds().get(0),
                    image.getId(),
                    image.getImageId(),
                    null);

            VolumeBitmapCommandParameters parameters =
                    new VolumeBitmapCommandParameters(
                            getStoragePoolId(),
                            locationInfo,
                            vmCheckpoint.getId().toString());
            parameters.setVdsId(vdsId);
            parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());

            ActionReturnValue returnValue = runInternalActionWithTasksContext(ActionType.RemoveVolumeBitmap, parameters);
            if (!returnValue.getSucceeded()) {
                log.error("Failed to remove checkpoint '{}' bitmap '{}' from disk '{}'",
                        vmCheckpoint.getId(),
                        vmCheckpoint.getId(),
                        image.getId());
                bitmapsRemoved = false;
            }
        }
        return bitmapsRemoved;
    }

    private void updateCheckpointsChainInDb() {
        VmCheckpoint childVmCheckpoint = vmCheckpointDao.getChildCheckpoint(vmCheckpoint.getId());
        TransactionSupport.executeInNewTransaction(() -> {
            if (childVmCheckpoint != null) {
                // Removed checkpoint isn't the leaf checkpoint.
                childVmCheckpoint.setParentId(vmCheckpoint.getParentId());
                vmCheckpointDao.update(childVmCheckpoint);
            }
            vmCheckpointDao.remove(vmCheckpoint.getId());
            return null;
        });
    }

    private boolean redefineVmCheckpoint() {
        VmBackup vmBackup = new VmBackup();
        vmBackup.setFromCheckpointId(vmCheckpoint.getId());
        vmBackup.setDisks(vmCheckpoint.getDisks());
        vmBackup.setVmId(getVmId());

        VmBackupParameters vmBackupParameters = new VmBackupParameters(vmBackup);
        vmBackupParameters.setParentCommand(getActionType());
        vmBackupParameters.setParentParameters(getParameters());
        vmBackupParameters.setEndProcedure(ActionParametersBase.EndProcedure.PARENT_MANAGED);

        log.info("Redefine VM checkpoint '{}' for VM '{}'", vmCheckpoint.getId(), getVmId());
        ActionReturnValue returnValue = runInternalAction(ActionType.RedefineVmCheckpoint, vmBackupParameters);
        return returnValue.getSucceeded();
    }

    protected DiskExistenceValidator createDiskExistenceValidator(Set<Guid> disksGuids) {
        return Injector.injectMembers(new DiskExistenceValidator(disksGuids));
    }

    protected DiskImagesValidator createDiskImagesValidator(List<DiskImage> disks) {
        return Injector.injectMembers(new DiskImagesValidator(disks));
    }

    public Set<Guid> getDiskIds() {
        return (vmCheckpoint != null && vmCheckpoint.getDisks() != null) ?
                vmCheckpoint.getDisks()
                        .stream()
                        .map(DiskImage::getId)
                        .collect(Collectors.toCollection(LinkedHashSet::new)) :
                Collections.emptySet();
    }

    private boolean isLiveDeleteCheckpoint() {
        return getVm().getStatus() == VMStatus.Up;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__CHECKPOINT);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        vmCheckpoint.getDisks().forEach(
                disk -> permissionList.add(
                        new PermissionSubject(disk.getId(), VdcObjectType.Disk, ActionGroup.BACKUP_DISK)));
        return permissionList;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVmCheckpoint().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM,
                        EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_A_BACKUP_CHECKPOINT_REMOVAL));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVm().getName());
        addCustomValue("checkpointId", getParameters().getVmCheckpoint().getId().toString());
        return getSucceeded() ? AuditLogType.VM_CHECKPOINT_DELETED : AuditLogType.VM_CHECKPOINT_FAILED_TO_DELETE;
    }
}
