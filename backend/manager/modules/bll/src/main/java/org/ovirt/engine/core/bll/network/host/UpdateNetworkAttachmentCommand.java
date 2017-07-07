package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.NetworkAttachmentParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

@NonTransactiveCommandAttribute
public class UpdateNetworkAttachmentCommand<T extends NetworkAttachmentParameters> extends VdsCommand<T> {


    public UpdateNetworkAttachmentCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        addValidationGroup(UpdateEntity.class);
    }

    @Override
    protected boolean validate() {
        NetworkAttachment networkAttachment = getParameters().getNetworkAttachment();
        if (networkAttachment == null) {
            return failValidation(EngineMessage.NETWORK_ATTACHMENT_NOT_SPECIFIED);
        }

        if (networkAttachment.getId() == null) {
            return failValidation(EngineMessage.NETWORK_ATTACHMENT_WHEN_UPDATING_YOU_HAVE_TO_PROVIDE_ID);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        HostSetupNetworksParameters params = new HostSetupNetworksParameters(getParameters().getVdsId());
        params.getNetworkAttachments().add(getParameters().getNetworkAttachment());
        ActionReturnValue returnValue = runInternalAction(ActionType.HostSetupNetworks, params);
        propagateFailure(returnValue);
        setSucceeded(returnValue.getSucceeded());
    }
}
