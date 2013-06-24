package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ExternalNetwork extends EntityModel {

    private Network network;
    private ListModel dcList;
    private boolean publicUse;

    public ExternalNetwork() {
        dcList = new ListModel();
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public ListModel getDataCenters() {
        return dcList;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }

}
