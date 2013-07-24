package org.ovirt.engine.core.bll;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class ChangeDiskCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {
    private String mCdImagePath;

    public ChangeDiskCommand(T parameters) {
        super(parameters);
        mCdImagePath = ImagesHandler.cdPathWindowsToLinux(parameters.getCdImagePath(), getVm().getStoragePoolId());
    }

    public String getDiskName() {
        return new File(mCdImagePath).getName();
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
        }

        if (retValue && !getVm().isRunningOrPaused()) {
            setSucceeded(false);
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);

            // An empty 'mCdImagePath' means eject CD
            if (!StringUtils.isEmpty(mCdImagePath)) {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            } else {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EJECT_CD);
            }
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else if ((IsoDomainListSyncronizer.getInstance().findActiveISODomain(getVm().getStoragePoolId()) == null)
                && !StringUtils.isEmpty(mCdImagePath)) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            setSucceeded(false);
            retValue = false;
        }
        return retValue;
    }

    @Override
    protected void Perform() {
        setActionReturnValue(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.ChangeDisk,
                        new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), mCdImagePath))
                .getReturnValue());
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? "".equals(mCdImagePath) ? AuditLogType.USER_EJECT_VM_DISK
                : AuditLogType.USER_CHANGE_DISK_VM : AuditLogType.USER_FAILED_CHANGE_DISK_VM;
    }
}
