package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolModel;

import java.util.List;

public class PoolModelBehaviorBaseTest extends BaseVmModelBehaviorTest {

    VmBase vm = new VmBase();

    @Override
    protected VmBase getVm() {
        return vm;
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new PoolModelBehaviorBase() {
            @Override
            protected List<VDSGroup> filterClusters(List<VDSGroup> clusters) {
                return null;
            }
        };
    }

    @Override
    protected UnitVmModel createModel(VmModelBehaviorBase behavior) {
        final VDSGroup cluster = new VDSGroup();
        cluster.setCompatibilityVersion(CLUSTER_VERSION);

        UnitVmModel model = new PoolModel(behavior) {
            @Override
            public VDSGroup getSelectedCluster() {
                return cluster;
            }
        };

        mockAsyncDataProvider(model);

        return model;
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCore(model);
    }
}
