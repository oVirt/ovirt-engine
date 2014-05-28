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
        return new PoolModel(behavior);
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCore(model);
    }
}
