package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class DedicatedVmForVdsUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        // host migration configuration
        List<VDS> defaultHosts = model.getDefaultHost().getSelectedItems();
        if (model.getIsAutoAssign().getEntity()) {
            vm.setDedicatedVmForVdsList(Collections.<Guid>emptyList());
        } else {
            List<Guid> defaultHostsGuids = new ArrayList<>();
            for (VDS host: defaultHosts) {
                defaultHostsGuids.add(host.getId());
            }
            vm.setDedicatedVmForVdsList(defaultHostsGuids);
        }
    }
}
