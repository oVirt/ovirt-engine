package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class DetachGlusterHostsModel extends Model {

    ListModel hosts;
    EntityModel force;

    public DetachGlusterHostsModel()
    {
        setHosts(new ListModel());
        setForce(new EntityModel());
        getForce().setEntity(Boolean.FALSE);
    }

    public ListModel getHosts() {
        return hosts;
    }

    public void setHosts(ListModel hosts) {
        this.hosts = hosts;
    }

    public EntityModel getForce() {
        return force;
    }

    public void setForce(EntityModel force) {
        this.force = force;
    }

    public boolean validate() {
        boolean valid = true;
        if (hosts.getSelectedItems() == null || hosts.getSelectedItems().size() < 1) {
            valid = false;
        }
        return valid;
    }
}
