package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
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
        setVdsId(getVm().getRunOnVds());
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        log.info("Stopping VmBackup '{}'", vmBackup.getId());
        if (stopVmBackup()) {
            vmBackup.setPhase(VmBackupPhase.FINALIZING);
            vmBackupDao.update(vmBackup);
        } else {
            log.info("Failed to stop VmBackup '{}'", vmBackup.getId());
        }
        setSucceeded(true);
    }

    private boolean stopVmBackup() {
        VDSReturnValue vdsRetVal;
        try {
            getParameters().getVmBackup().setDisks(
                    vmBackupDao.getDisksByBackupId(getVmId()));
            vdsRetVal = runVdsCommand(VDSCommandType.StopVmBackup,
                    new VmBackupVDSParameters(getVdsId(), getParameters().getVmBackup()));
            if (!vdsRetVal.getSucceeded()) {
                EngineException engineException = new EngineException();
                engineException.setVdsError(vdsRetVal.getVdsError());
                throw engineException;
            }
            return true;
        } catch (EngineException e) {
            log.error("Failed to execute VM.stopBackup: {}", e);
            return false;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__STOP);
        addValidationMessage(EngineMessage.VAR__ACTION__BACKUP);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        vmBackup.getDisks().forEach(
                disk -> permissionList.add(
                        new PermissionSubject(disk.getId(), VdcObjectType.Disk, ActionGroup.BACKUP_DISK)));
        return permissionList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVm().getName());
        return getSucceeded() ? AuditLogType.VM_BACKUP_FINALIZED : AuditLogType.VM_BACKUP_FAILED_TO_FINALIZE;
    }
}
