package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("DiskName") })
public class ChangeDiskCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {
    private String mCdImagePath;

    public ChangeDiskCommand(T parameters) {
        super(parameters);
        mCdImagePath = ImagesHandler.cdPathWindowsToLinux(parameters.getCdImagePath(), getVm().getstorage_pool_id());
    }

    public String getDiskName() {
        return Path.GetFileName(mCdImagePath);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (!VM.isStatusUpOrPaused(getVm().getstatus())) {
            setSucceeded(false);
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);

            // An empty 'mCdImagePath' means eject CD
            if (!StringHelper.isNullOrEmpty(mCdImagePath)) {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CHANGE_CD);
            } else {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EJECT_CD);
            }
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else if ((RunVmCommand.findActiveISODomain(getVm().getstorage_pool_id()) == null) && !StringHelper.isNullOrEmpty(mCdImagePath)) {
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
                        new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getvm_guid(), mCdImagePath))
                .getReturnValue());
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? StringHelper.EqOp(mCdImagePath, "") ? AuditLogType.USER_EJECT_VM_DISK
                : AuditLogType.USER_CHANGE_DISK_VM : AuditLogType.USER_FAILED_CHANGE_DISK_VM;
    }
}
