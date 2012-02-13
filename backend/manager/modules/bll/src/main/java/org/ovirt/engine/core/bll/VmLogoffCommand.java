package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.LogoffVmParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogoffVDSCommandParameters;

@InternalCommandAttribute
public class VmLogoffCommand<T extends LogoffVmParameters> extends VmOperationCommandBase<T> {
    private boolean mForce;

    public VmLogoffCommand(T parameters) {
        super(parameters);
        mForce = parameters.getForce();
    }

    @Override
    protected void Perform() {
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.VmLogoff,
                        new VmLogoffVDSCommandParameters(getVdsId(), getVm().getId(), mForce)).getSucceeded());
    }
}
