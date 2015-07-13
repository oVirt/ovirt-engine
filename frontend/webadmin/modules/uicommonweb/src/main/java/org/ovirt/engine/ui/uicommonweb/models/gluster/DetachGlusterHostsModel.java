package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class DetachGlusterHostsModel extends Model {

    ListModel<EntityModel<String>> hosts;
    EntityModel<Boolean> force;

    public DetachGlusterHostsModel() {
        setHosts(new ListModel<EntityModel<String>>());
        setForce(new EntityModel<Boolean>());
        getForce().setEntity(Boolean.FALSE);
    }

    public ListModel<EntityModel<String>> getHosts() {
        return hosts;
    }

    public void setHosts(ListModel<EntityModel<String>> hosts) {
        this.hosts = hosts;
    }

    public EntityModel<Boolean> getForce() {
        return force;
    }

    public void setForce(EntityModel<Boolean> force) {
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
