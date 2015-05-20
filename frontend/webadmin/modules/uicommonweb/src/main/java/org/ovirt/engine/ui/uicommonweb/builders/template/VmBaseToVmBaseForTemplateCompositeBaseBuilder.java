package org.ovirt.engine.ui.uicommonweb.builders.template;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CpuSharesVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.DedicatedVmForVdsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MigrationOptionsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UsbPolicyVmBaseToVmBaseBuilder;

public class VmBaseToVmBaseForTemplateCompositeBaseBuilder extends CompositeSyncBuilder<VmBase, VmBase> {

    public VmBaseToVmBaseForTemplateCompositeBaseBuilder() {
        super(
                new KernelParamsVmBaseToVmBaseBuilder(),
                new DedicatedVmForVdsVmBaseToVmBaseBuilder(),
                new MigrationOptionsVmBaseToVmBaseBuilder(),
                new UsbPolicyVmBaseToVmBaseBuilder(),
                new CpuSharesVmBaseToVmBaseBuilder()
        );
    }
}
