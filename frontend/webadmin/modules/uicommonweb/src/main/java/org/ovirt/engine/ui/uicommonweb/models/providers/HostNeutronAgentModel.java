package org.ovirt.engine.ui.uicommonweb.models.providers;

public class HostNeutronAgentModel extends NeutronAgentModel {

    public HostNeutronAgentModel() {
        getPluginType().setIsChangable(false);
        getQpidHost().setIsChangable(false);
        getQpidPort().setIsChangable(false);
        getQpidUsername().setIsChangable(false);
        getQpidPassword().setIsChangable(false);
    }

}
