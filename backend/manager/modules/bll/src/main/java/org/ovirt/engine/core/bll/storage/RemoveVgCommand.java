package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVgParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.vdscommands.RemoveVGVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class RemoveVgCommand<T extends RemoveVgParameters> extends StorageHandlingCommandBase<T> {
    public RemoveVgCommand(T parameters) {
        super(parameters);
    }

    public String getVgId() {
        return getParameters().getVgId();
    }

    @Override
    protected void executeCommand() {
        VdcActionParametersBase tempVar = getParameters();
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.RemoveVG,
                        new RemoveVGVDSCommandParameters(getParameters().getVdsId(),
                                ((RemoveVgParameters) ((tempVar instanceof RemoveVgParameters) ? tempVar : null))
                                        .getVgId())).getSucceeded());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VG : AuditLogType.USER_REMOVE_VG_FAILED;
    }
}
