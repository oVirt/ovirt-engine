package org.ovirt.engine.ui.uicommonweb.models.hosts;

public class HostManagementNetworkModel extends HostInterfaceModel {

    public HostManagementNetworkModel() {
        getGateway().setIsAvailable(true);
    }
}
