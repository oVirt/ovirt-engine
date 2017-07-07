package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkAttachmentParameters;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;

@NonTransactiveCommandAttribute
public class RemoveNetworkAttachmentCommand<T extends RemoveNetworkAttachmentParameters> extends VdsCommand<T> {

    public RemoveNetworkAttachmentCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        addValidationGroup(RemoveEntity.class);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(getParameters().getVdsId());
        params.getRemovedNetworkAttachments().add(getParameters().getNetworkAttachmentId());
        ActionReturnValue returnValue = runInternalAction(ActionType.HostSetupNetworks, params);
        propagateFailure(returnValue);
        setSucceeded(returnValue.getSucceeded());
    }
}
