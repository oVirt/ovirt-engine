package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class VmAffinityGroupListModel extends AffinityGroupListModel<VM> {

    public VmAffinityGroupListModel() {
        super(QueryType.GetAffinityGroupsByVmId);
    }

    @Override
    protected AffinityGroup getNewAffinityGroup() {
        AffinityGroup affinityGroup = super.getNewAffinityGroup();
        affinityGroup.setVmIds(new ArrayList<Guid>());
        affinityGroup.getVmIds().add(getEntity().getId());
        return affinityGroup;
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
