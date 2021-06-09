package org.ovirt.engine.core.bll.storage.backup;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.dao.VmBackupDao;

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
        if (vmBackup.getPhase() != VmBackupPhase.READY) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_BACKUP_NOT_READY,
                    String.format("$vmBackupPhase %s", vmBackup.getPhase()));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        log.info("Stopping VmBackup '{}'", vmBackup.getId());
        if (stopVmBackup()) {
            updateVmBackupPhase(VmBackupPhase.FINALIZING);
            setSucceeded(true);
        } else {
            log.error("Failed to stop VmBackup '{}'", vmBackup.getId());
            updateVmBackupPhase(VmBackupPhase.FAILED);
        }
    }

    private boolean stopVmBackup() {
        if (isColdBackup()) {
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
            log.error("Failed to execute VM.stopBackup operation", e);
            return false;
        }
    }

    private List<DiskImage> getDisks() {
        return getParameters().getVmBackup().getDisks() == null ?
                vmBackupDao.getDisksByBackupId(getParameters().getVmBackup().getId()) :
                getParameters().getVmBackup().getDisks();
    }

    private boolean isColdBackup() {
        return getVm().getStatus() == VMStatus.Down;
    }

    private void updateVmBackupPhase(VmBackupPhase phase) {
        vmBackup.setPhase(phase);
        vmBackupDao.update(vmBackup);
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
