package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class ClusterAffinityGroupListModel extends AffinityGroupListModel<VDSGroup> {

    public ClusterAffinityGroupListModel() {
        super(VdcQueryType.GetAffinityGroupsByClusterId);
    }

    @Override
    protected Guid getClusterId() {
        return getEntity().getId();
    }

    @Override
    protected String getClusterName() {
        return getEntity().getName();
    }
}
