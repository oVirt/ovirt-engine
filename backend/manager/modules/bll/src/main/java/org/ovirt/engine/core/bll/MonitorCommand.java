package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import org.ovirt.engine.core.common.action.MonitorCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmMonitorCommandVDSCommandParameters;

@InternalCommandAttribute
public class MonitorCommand<T extends MonitorCommandParameters> extends VmOperationCommandBase<T> {
    private String mMonitorCommand;

    public MonitorCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
