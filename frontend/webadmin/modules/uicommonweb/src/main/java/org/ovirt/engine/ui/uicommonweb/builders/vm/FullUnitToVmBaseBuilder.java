package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Builder that performs FULL mapping of UnitVmModel to VM/Template.
 * Typically used when saving or cloning existing VM/Template.
 *
 * Note that if you are adding a new property mapping, please consider that more appropriate place for it may be
 * in {@link CommonUnitToVmBaseBuilder} if this mapping should be also performed in onNewTemplate actions
 * or further yet in {@link CoreUnitToVmBaseBuilder} if this mapping should also apply to pool Vms.
 * (most fields fall into the "Core" category)
 */
public class FullUnitToVmBaseBuilder<T extends VmBase> extends CompositeSyncBuilder<UnitVmModel, T> {
    public FullUnitToVmBaseBuilder() {
        super(
                new CommonUnitToVmBaseBuilder(),
                new NameUnitToVmBaseBuilder(),
                new DedicatedVmForVdsUnitToVmBaseBuilder(),
                new KernelParamsUnitToVmBaseBuilder(),
                new MigrationOptionsUnitToVmBaseBuilder(),
                new UsbPolicyUnitToVmBaseBuilder()
        );
    }
}
