package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VfsConfigNetwork extends EntityModel<Network> {
    boolean isAttached;
    String labelViaAttached;

    public VfsConfigNetwork(boolean isAttached, String labelViaAttached, Network network) {
        this.isAttached = isAttached;
        this.labelViaAttached = labelViaAttached;
        setEntity(network);
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void setAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }

    public String getLabelViaAttached() {
        return labelViaAttached;
    }

    public void setLabelViaAttached(String labelViaAttached) {
        this.labelViaAttached = labelViaAttached;
    }
}
