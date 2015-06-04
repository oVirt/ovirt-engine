package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;

public class DedicatedVmForVdsVmBaseToVmBaseBuilder extends BaseSyncBuilder<VmBase, VmBase> {
    @Override
    protected void build(VmBase source, VmBase destination) {
        destination.setDedicatedVmForVdsList(source.getDedicatedVmForVdsList());
    }
}
