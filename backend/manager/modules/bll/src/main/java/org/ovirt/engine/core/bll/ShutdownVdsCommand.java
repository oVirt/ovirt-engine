package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsShutdownParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ShutdownVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class ShutdownVdsCommand<T extends VdsShutdownParameters> extends VdsCommand<T> {
    public ShutdownVdsCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected void executeCommand() {
        VDSReturnValue result = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.ShutdownVds,
                        new ShutdownVdsVDSCommandParameters(getVds().getId(), getParameters().getReboot()));
        if (result == null || (VDSStatus) result.getReturnValue() != VDSStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.VDS_SHUTDOWN_ERROR);
        }
        getVds().setStatus((VDSStatus) result.getReturnValue());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_SHUTDOWN_VDS_NOT_FOUND);
            returnValue = false;
        }
        if (getVds().getStatus() == VDSStatus.Down || getVds().getStatus() == VDSStatus.NonResponsive) {
            addCanDoActionMessage(VdcBllMessages.VDS_SHUTDOWN_NO_RESPONSE);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_SHUTDOWN : AuditLogType.USER_FAILED_VDS_SHUTDOWN;
    }
}
