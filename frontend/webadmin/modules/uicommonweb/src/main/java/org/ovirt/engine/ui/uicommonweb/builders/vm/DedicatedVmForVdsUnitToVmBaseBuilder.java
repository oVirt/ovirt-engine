package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class DedicatedVmForVdsUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        if (model.getIsAutoAssign().getEntity()) {
            vm.setDedicatedVmForVdsList(Collections.emptyList());
        } else {
            // host migration configuration
            List<Guid> defaultHostsGuids = model
                    .getDefaultHost()
                    .getSelectedItems()
                    .stream()
                    .map(VDS::getId)
                    .collect(Collectors.toList());
            vm.setDedicatedVmForVdsList(defaultHostsGuids);
        }
    }
}
