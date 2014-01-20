package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;


public class KernelParamsVmBaseToVmBaseBuilder extends BaseSyncBuilder<VmBase, VmBase> {
    @Override
    protected void build(VmBase source, VmBase destination) {
        destination.setInitrdUrl(source.getInitrdUrl());
        destination.setKernelUrl(source.getKernelUrl());
        destination.setKernelParams(source.getKernelParams());
    }
}
