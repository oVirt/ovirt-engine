package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Used when creating / editing Pools.
 *
 */
public class PoolUnitToVmBaseBuilder<T extends VmBase> extends CompositeSyncBuilder<UnitVmModel, T> {

    public PoolUnitToVmBaseBuilder() {
        super(
                new CoreUnitToVmBaseBuilder(),
                new NameUnitToVmBaseBuilder(),
                new KernelParamsUnitToVmBaseBuilder(),
                new MigrationOptionsUnitToVmBaseBuilder(),
                new DedicatedVmForVdsUnitToVmBaseBuilder(),
                new UsbPolicyUnitToVmBaseBuilder()
        );
    }
}
