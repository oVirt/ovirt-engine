package org.ovirt.engine.core.bll.storage.lease;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveVmLeaseCommand<T extends VmLeaseParameters> extends VmLeaseCommandBase<T> {

    public RemoveVmLeaseCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveVmLeaseCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected VDSCommandType getLeaseAction() {
        return VDSCommandType.RemoveVmLease;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.removeLease;
    }
}
