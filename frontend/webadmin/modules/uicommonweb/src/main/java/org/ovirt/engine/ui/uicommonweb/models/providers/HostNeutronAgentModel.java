package org.ovirt.engine.ui.uicommonweb.models.providers;

public class HostNeutronAgentModel extends NeutronAgentModel {

    public HostNeutronAgentModel() {
        getPluginType().setIsChangeable(false);
        getBrokerType().setIsChangeable(false);
        getMessagingServer().setIsChangeable(false);
        getMessagingServerPort().setIsChangeable(false);
        getMessagingServerUsername().setIsChangeable(false);
        getMessagingServerPassword().setIsChangeable(false);
    }

}
