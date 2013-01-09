package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.Path;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("DiskName") })
public class ChangeFloppyCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {
    private String mCdImagePath;

    public ChangeFloppyCommand(T parameters) {
        super(parameters);
        mCdImagePath = ImagesHandler.cdPathWindowsToLinux(parameters.getCdImagePath(), getVm().getStoragePoolId());
    }

    public String getDiskName() {
        return Path.GetFileName(mCdImagePath);
    }

    @Override
    protected void Perform() {
        if (getVm().isRunningOrPaused()) {
            setActionReturnValue(Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.ChangeFloppy,
                            new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), mCdImagePath))
                    .getReturnValue());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? StringHelper.EqOp(mCdImagePath, "") ? AuditLogType.USER_EJECT_VM_FLOPPY
                : AuditLogType.USER_CHANGE_FLOPPY_VM : AuditLogType.USER_FAILED_CHANGE_FLOPPY_VM;
    }
}
