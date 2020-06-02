package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmCheckpointParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.di.Injector;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class DeleteVmCheckpointCommand<T extends VmCheckpointParameters> extends VmCommand<T> {

    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private VmBackupDao vmBackupDao;

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

        if (vmCheckpoint.getParentId() != null) {
            return failValidation(EngineMessage.CANNOT_DELETE_NON_ROOT_CHECKPOINT,
                    String.format("$checkpointId %s", getParameters().getVmCheckpoint().getId()));
        }

        if (!getVm().getStatus().isQualifiedForVmBackup()) {
            return failValidation(EngineMessage.CANNOT_DELETE_CHECKPOINT_VM_SHOULD_BE_IN_UP_STATUS);
        }

        if (!vmBackupDao.getAllForVm(getVmId()).isEmpty()) {
            return failValidation(EngineMessage.CANNOT_START_BACKUP_ALREADY_IN_PROGRESS);
        }

        DiskExistenceValidator diskExistenceValidator = createDiskExistenceValidator(getDiskIds());
        if (!validate(diskExistenceValidator.disksNotExist())) {
            return false;
        }

        DiskImagesValidator diskImagesValidator = createDiskImagesValidator(vmCheckpoint.getDisks());
        if (!validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        log.info("Deleting VmCheckpoint '{}'", vmCheckpoint.getId());
        if (deleteVmCheckpoint()) {
            vmCheckpointDao.remove(vmCheckpoint.getId());
            updateNewRootCheckpointXML();
        } else {
            log.error("Failed to delete VmCheckpoint '{}'", vmCheckpoint.getId());
        }
        setSucceeded(true);
    }

    private boolean deleteVmCheckpoint() {
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
            return true;
        } catch (EngineException e) {
            log.error("Failed to execute VM.delete_checkpoints: {}", e.getMessage());
            return false;
        }
    }

    private void updateNewRootCheckpointXML() {
        // TODO: get new root checkpoint and update the XML
        // need VDSM support for implementing get checkpoint XML
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

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__ACTION__CHECKPOINT);
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
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getParameters().getVmCheckpoint().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
        return locks;
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
