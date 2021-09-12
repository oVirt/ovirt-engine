package org.ovirt.engine.core.bll.storage.backup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class StopVmBackupCommand<T extends VmBackupParameters> extends VmCommand<T> {

    @Inject
    private VmBackupDao vmBackupDao;

    private VmBackup vmBackup;

    public StopVmBackupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        vmBackup = vmBackupDao.get(getParameters().getVmBackup().getId());
        vmBackup.setDisks(vmBackupDao.getDisksByBackupId(vmBackup.getId()));
        setVmId(vmBackup.getVmId());
        setVdsId(vmBackup.getHostId());
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        log.info("Stopping backup '{}'", vmBackup.getId());

        try (EngineLock backupLock = getEntityUpdateLock(vmBackup.getId())) {
            lockManager.acquireLockWait(backupLock);

            // Update the backup entity from the DB to be sure that we have the most up-to-date backup phase.
            vmBackup = vmBackupDao.get(getParameters().getVmBackup().getId());
            vmBackup.setDisks(vmBackupDao.getDisksByBackupId(vmBackup.getId()));

            VmBackupPhase phase = vmBackup.getPhase();
            if (phase.isBackupFinalizing() || phase.isBackupFinished()) {
                String errorMsg = String.format(
                        "VM '%s' backup '%s' is already in '%s' phase, no need to stop the backup again",
                        vmBackup.getVmId(), vmBackup.getId(), phase);
                log.warn(errorMsg);
                getReturnValue().setExecuteFailedMessages(new ArrayList<>(Collections.singleton(errorMsg)));
                return;
            }

            if (stopVmBackup()) {
                if (phase == VmBackupPhase.READY) {
                    updateVmBackupPhase(VmBackupPhase.FINALIZING);
                    setSucceeded(true);
                } else {
                    // Premature backup finish.
                    String errorMsg = String.format(
                            "Backup '%s' is in '%s' phase, premature backup stop was initiated.",
                            vmBackup.getId(), phase);
                    log.warn(errorMsg);
                    getReturnValue().setExecuteFailedMessages(new ArrayList<>(Collections.singleton(errorMsg)));
                    updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
                }
            } else {
                log.error("Failed to stop backup '{}' in '{}' phase", vmBackup.getId(), phase);
                updateVmBackupPhase(VmBackupPhase.FINALIZING_FAILURE);
            }
        }
    }

    private boolean stopVmBackup() {
        if (!isLiveBackup()) {
            return true;
        }

        try {
            getParameters().getVmBackup().setDisks(getDisks());
            VDSReturnValue vdsRetVal = runVdsCommand(VDSCommandType.StopVmBackup,
                    new VmBackupVDSParameters(getVdsId(), getParameters().getVmBackup()));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            return true;
        } catch (EngineException e) {
            log.error("Failed to execute stop VM backup operation", e);
            return false;
        }
    }

    private List<DiskImage> getDisks() {
        return getParameters().getVmBackup().getDisks() == null ?
                vmBackupDao.getDisksByBackupId(getParameters().getVmBackup().getId()) :
                getParameters().getVmBackup().getDisks();
    }

    private boolean isLiveBackup() {
        return vmBackup.isLiveBackup();
    }

    private void updateVmBackupPhase(VmBackupPhase phase) {
        log.info("Change VM '{}' backup '{}' phase from '{}' to '{}'",
                vmBackup.getVmId(), vmBackup.getId(), vmBackup.getPhase(), phase);
        vmBackup.setPhase(phase);
        vmBackupDao.update(vmBackup);
    }

    private EngineLock getEntityUpdateLock(Guid backupId) {
        Map<String, Pair<String, String>> lockMap = Collections.singletonMap(
                backupId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_BACKUP, EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_LOCKED));
        return new EngineLock(lockMap);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__STOP);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_BACKUP);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        vmBackup.getDisks().forEach(
                disk -> permissionList.add(
                        new PermissionSubject(disk.getId(), VdcObjectType.Disk, ActionGroup.BACKUP_DISK)));
        return permissionList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVm().getName());
        addCustomValue("backupId", getParameters().getVmBackup().getId().toString());
        return getSucceeded() ? AuditLogType.VM_BACKUP_FINALIZED : AuditLogType.VM_BACKUP_FAILED_TO_FINALIZE;
    }
}
