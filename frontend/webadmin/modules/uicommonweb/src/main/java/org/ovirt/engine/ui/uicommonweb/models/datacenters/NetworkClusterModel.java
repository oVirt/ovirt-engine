package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class NetworkClusterModel extends EntityModel {

    private boolean attached;
    private boolean required;

    public NetworkClusterModel(Cluster cluster) {
        setEntity(cluster);
    }

    @Override
    public Cluster getEntity() {
        return (Cluster) super.getEntity();
    }

    public String getName() {
        return getEntity().getName();
    }

    public boolean isAttached() {
        return attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
