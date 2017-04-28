package org.ovirt.engine.ui.uicommonweb.models.configure.labels.list;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class HostAffinityLabelListModel extends AffinityLabelListModel<VDS> {

    public HostAffinityLabelListModel() {
        super(VdcQueryType.GetLabelByEntityId);
    }

    @Override
    protected Label getNewAffinityLabel() {
        Label affinityLabel = super.getNewAffinityLabel();
        affinityLabel.addHost(getEntity());
        return affinityLabel;
    }

    @Override
    protected Guid getClusterId() {
        return getEntity().getClusterId();
    }

    @Override
    protected String getClusterName() {
        return getEntity().getClusterName();
    }
}
