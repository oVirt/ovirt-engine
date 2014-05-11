package org.ovirt.engine.ui.uicommonweb.models.providers;

public class HostNeutronAgentModel extends NeutronAgentModel {

    public HostNeutronAgentModel() {
        getPluginType().setIsChangable(false);
        getBrokerType().setIsChangable(false);
        getMessagingServer().setIsChangable(false);
        getMessagingServerPort().setIsChangable(false);
        getMessagingServerUsername().setIsChangable(false);
        getMessagingServerPassword().setIsChangable(false);
    }

}
