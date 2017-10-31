package org.ovirt.engine.core.bll.provider.network;

import java.util.Arrays;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalSubnetParameters;
import org.ovirt.engine.core.common.action.AddNetworkWithSubnetParameters;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class AddNetworkWithSubnetOnProviderCommand<T extends AddNetworkWithSubnetParameters>
    extends AddNetworkOnProviderCommand<T> {

    public AddNetworkWithSubnetOnProviderCommand(
        T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void postAddNetwork(Guid providerId, String externalId) {
        ExternalSubnet externalSubnet = getParameters().getExternalSubnet();
        if (externalSubnet != null) {
            AddExternalSubnetParameters subnetParameters = new AddExternalSubnetParameters(
                externalSubnet, providerId, externalId);
            backend.runInternalMultipleActions(ActionType.AddSubnetToProvider, Arrays.asList(subnetParameters));
        }
    }
}
