package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolModel;

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
            protected List<Cluster> filterClusters(List<Cluster> clusters) {
                return null;
            }
        };
    }

    @Override
    protected UnitVmModel createModel(VmModelBehaviorBase behavior) {
        final Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(CLUSTER_VERSION);

        UnitVmModel model = new PoolModel(behavior) {
            @Override
            public Cluster getSelectedCluster() {
                return cluster;
            }
        };

        return model;
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCore(model);
    }
}
