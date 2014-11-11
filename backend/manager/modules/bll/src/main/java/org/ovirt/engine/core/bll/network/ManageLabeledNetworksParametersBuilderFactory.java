package org.ovirt.engine.core.bll.network;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;

@Singleton
public class ManageLabeledNetworksParametersBuilderFactory {

    public ManageLabeledNetworksParametersBuilder create(CommandContext commandContext) {
        return new ManageLabeledNetworksParametersBuilderImpl(commandContext);
    }
}
