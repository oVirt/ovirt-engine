package org.ovirt.engine.core.bll.storage.lease;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmLeaseParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddVmLeaseCommand<T extends VmLeaseParameters> extends VmLeaseCommandBase<T> {

    public AddVmLeaseCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public AddVmLeaseCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected VDSCommandType getLeaseAction() {
        return VDSCommandType.AddVmLease;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.addVmLease;
    }
}
