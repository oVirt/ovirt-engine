package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VfsConfigNetwork extends EntityModel<Network> {
    boolean isAttached;
    VfsNicLabelModel labelModel;

    public VfsConfigNetwork(boolean isAttached, VfsNicLabelModel labelModel, Network network) {
        this.isAttached = isAttached;
        this.labelModel = labelModel;
        setEntity(network);
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void setAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }

    public String getLabelViaAttached() {
        return labelModel.computeSelectedLabels().contains(getEntity().getLabel()) ? getEntity().getLabel()
                : null;
    }

    public boolean isAttachedViaLabel() {
        return getLabelViaAttached() != null;
    }
}
