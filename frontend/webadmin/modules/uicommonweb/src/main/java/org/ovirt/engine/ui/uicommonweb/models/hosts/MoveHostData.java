package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;


public class MoveHostData extends EntityModel<VDS> {

    boolean activateHost;

    public MoveHostData(VDS host) {
        setEntity(host);
    }

    public VDS getTemplate() {
        return getEntity();
    }

    public boolean getActivateHost() {
        return activateHost;
    }

    public void setActivateHost(boolean activateHost) {
        this.activateHost = activateHost;
    }

}
