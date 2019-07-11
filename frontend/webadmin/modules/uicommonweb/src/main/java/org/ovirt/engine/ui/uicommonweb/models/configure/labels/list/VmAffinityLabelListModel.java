package org.ovirt.engine.ui.uicommonweb.models.configure.labels.list;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmAffinityLabelListModel extends AffinityLabelListModel<VM> {

    public VmAffinityLabelListModel() {
        super(QueryType.GetLabelByEntityId);
    }

    @Override
    protected Label getNewAffinityLabel() {
        Label affinityLabel = super.getNewAffinityLabel();
        affinityLabel.addVm(getEntity());
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

    @Override
    protected Version getClusterCompatibilityVersion() {
        return getEntity().getClusterCompatibilityVersion();
    }
}
