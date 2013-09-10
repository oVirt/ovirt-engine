package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.MonitorCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmMonitorCommandVDSCommandParameters;

@InternalCommandAttribute
public class MonitorCommand<T extends MonitorCommandParameters> extends VmOperationCommandBase<T> {
    private String mMonitorCommand;

    public MonitorCommand(T parameters) {
        super(parameters);
        mMonitorCommand = parameters.getCommand();
    }

    @Override
    protected void perform() {
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.VmMonitorCommand,
                        new VmMonitorCommandVDSCommandParameters(getVdsId(), getVmId(), mMonitorCommand))
                .getSucceeded());
    }
}
