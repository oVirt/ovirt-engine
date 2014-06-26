package org.ovirt.engine.core.bll;

import java.io.File;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class ChangeFloppyCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {
    private String mCdImagePath;

    public ChangeFloppyCommand(T parameters) {
        super(parameters);
        mCdImagePath = ImagesHandler.cdPathWindowsToLinux(parameters.getCdImagePath(), getVm().getStoragePoolId(), getVm().getRunOnVds());
    }

    public String getDiskName() {
        return new File(mCdImagePath).getName();
    }

    @Override
    protected void perform() {
        if (getVm().isRunningOrPaused()) {
            setActionReturnValue(runVdsCommand(VDSCommandType.ChangeFloppy,
                            new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), mCdImagePath))
                    .getReturnValue());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? "".equals(mCdImagePath) ? AuditLogType.USER_EJECT_VM_FLOPPY
                : AuditLogType.USER_CHANGE_FLOPPY_VM : AuditLogType.USER_FAILED_CHANGE_FLOPPY_VM;
    }
}
