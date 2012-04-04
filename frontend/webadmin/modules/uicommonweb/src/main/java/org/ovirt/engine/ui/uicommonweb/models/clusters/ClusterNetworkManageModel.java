package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;

public class ClusterNetworkManageModel extends EntityModel {

    private boolean attached;
    private boolean vmNetwork;
    private boolean required;
    private boolean management;

    public ClusterNetworkManageModel(network network) {
        setEntity(network);
        if (HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(network.getname())) {
            setManagement(true);
        }
    }

    @Override
    public network getEntity() {
        return (network) super.getEntity();
    }

    public String getName() {
        return getEntity().getname();
    }

    public boolean isAttached() {
        return attached;
    }

    public boolean isDisplayNetwork() {
        return getEntity().getis_display();
    }

    public boolean isManagement() {
        return management;
    }

    public boolean isRequired() {
        return getEntity().isRequired();
    }

    public boolean isVmNetwork() {
        return vmNetwork;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public void setDisplayNetwork(boolean displayNetwork) {
        getEntity().setis_display(displayNetwork);
    }

    public void setManagement(boolean management) {
        this.management = management;
    }

    public void setRequired(boolean required) {
        getEntity().setRequired(required);
    }

    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

}
