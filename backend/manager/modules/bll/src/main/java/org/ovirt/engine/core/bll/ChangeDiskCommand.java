package org.ovirt.engine.core.bll;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class ChangeDiskCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {
    private String cdImagePath;

    public ChangeDiskCommand(T parameters) {
        super(parameters);
    }

    public String getDiskName() {
        return new File(cdImagePath).getName();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            retValue = false;
        }

        if (retValue && !canRunActionOnNonManagedVm()) {
            retValue = false;
        } else {
            cdImagePath = ImagesHandler.cdPathWindowsToLinux(getParameters().getCdImagePath(), getVm().getStoragePoolId(), getVm().getRunOnVds());
        }

        if (retValue && !getVm().isRunningOrPaused()) {
            setSucceeded(false);
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);

            // An empty 'cdImagePath' means eject CD
            if (!StringUtils.isEmpty(cdImagePath)) {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            } else {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EJECT_CD);
            }
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL, LocalizedVmStatus.from(getVm().getStatus()));
        } else if ((IsoDomainListSyncronizer.getInstance().findActiveISODomain(getVm().getStoragePoolId()) == null)
                && !StringUtils.isEmpty(cdImagePath)) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            setSucceeded(false);
            retValue = false;
        } else if (StringUtils.isNotEmpty(cdImagePath) && !cdImagePath.endsWith(ValidationUtils.ISO_SUFFIX)) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CDROM_DISK_FORMAT);
            setSucceeded(false);
            retValue = false;
        }
        return retValue;
    }

    @Override
    protected void perform() {
        setActionReturnValue(runVdsCommand(VDSCommandType.ChangeDisk,
                        new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), cdImagePath))
                .getReturnValue());
        VmHandler.updateCurrentCd(getVdsId(), getVm(), getParameters().getCdImagePath());
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? "".equals(cdImagePath) ? AuditLogType.USER_EJECT_VM_DISK
                : AuditLogType.USER_CHANGE_DISK_VM : AuditLogType.USER_FAILED_CHANGE_DISK_VM;
    }
}
