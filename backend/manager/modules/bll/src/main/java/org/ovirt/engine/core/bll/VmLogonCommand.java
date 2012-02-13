package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;

@InternalCommandAttribute
public class VmLogonCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    public VmLogonCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void Perform() {
        String domainController = getCurrentUser() != null ? getCurrentUser().getDomainControler() : "";
        String password = getCurrentUser() != null ? getCurrentUser().getPassword() : "";
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.VmLogon,
                        new VmLogonVDSCommandParameters(getVdsId(), getVm().getId(), domainController,
                                getUserName(), password)).getSucceeded());
    }
}
