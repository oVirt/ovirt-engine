package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;
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
                return Collections.emptyList();
            }
        };
    }

    @Override
    protected UnitVmModel createModel(VmModelBehaviorBase behavior) {
        return new PoolModel(behavior);
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCore(model);
    }
}
