package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLockVDSCommandParameters;

@InternalCommandAttribute
public class VmLockCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    public VmLockCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void Perform() {
        setSucceeded(Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.VmLock, new VmLockVDSCommandParameters(getVdsId(), getVm().getvm_guid()))
                .getSucceeded());
    }
}
