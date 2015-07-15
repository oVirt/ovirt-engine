package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.MonitorCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmMonitorCommandVDSCommandParameters;

@InternalCommandAttribute
public class MonitorCommand<T extends MonitorCommandParameters> extends VmOperationCommandBase<T> {
    private String monitorCommand;

    public MonitorCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        monitorCommand = parameters.getCommand();
    }

    @Override
    protected void perform() {
        setSucceeded(runVdsCommand(VDSCommandType.VmMonitorCommand,
                        new VmMonitorCommandVDSCommandParameters(getVdsId(), getVmId(), monitorCommand))
                .getSucceeded());
    }
}
