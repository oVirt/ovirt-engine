package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class MigrationOptionsUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        vm.setMigrationSupport(model.getMigrationMode().getSelectedItem());
        vm.setMigrationDowntime(model.getSelectedMigrationDowntime());
        vm.setMigrationPolicyId(model.getSelectedMigrationPolicy());
    }
}
