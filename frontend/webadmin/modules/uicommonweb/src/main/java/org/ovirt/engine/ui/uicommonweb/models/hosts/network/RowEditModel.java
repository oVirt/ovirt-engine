package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class RowEditModel extends EntityModel {

    public RowEditModel(NetworkInterfaceModel nic) {
        setEntity(nic);
    }

    @Override
    public NetworkInterfaceModel getEntity() {
        return (NetworkInterfaceModel) super.getEntity();
    }
}
