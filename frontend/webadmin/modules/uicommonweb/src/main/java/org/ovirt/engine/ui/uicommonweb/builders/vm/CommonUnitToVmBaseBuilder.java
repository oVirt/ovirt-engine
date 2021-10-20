package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Maps properties that are common to non-pool VMs and Templates. Thus explicitly is used
 * only when New Template dialogue is created (in VmListModel).
 *
 * This builder is further subsumed by {@link FullUnitToVmBaseBuilder}
 * that performs full mapping of VmBase fields.
 */
public class CommonUnitToVmBaseBuilder<T extends VmBase> extends CompositeSyncBuilder<UnitVmModel, T> {
    public CommonUnitToVmBaseBuilder() {
        super(
                new CoreUnitToVmBaseBuilder(),
                new QuotaUnitToVmBaseBuilder<T>(),
                new NumaUnitToVmBaseBuilder<T>()

        );
    }

    @Override
    protected void postBuild(UnitVmModel model, VmBase vm) {
        // General
        vm.setComment(model.getComment().getEntity());
        vm.setDescription(model.getDescription().getEntity());
        vm.setStateless(model.getIsStateless().getEntity());
        vm.setRunAndPause(model.getIsRunAndPause().getEntity());
        // High availability
        VmResumeBehavior selectedResumeBehavior = model.getResumeBehavior().getSelectedItem();
        if (selectedResumeBehavior == null) {
            // the default
            vm.setResumeBehavior(VmResumeBehavior.AUTO_RESUME);
        } else {
            vm.setResumeBehavior(selectedResumeBehavior);
        }
        vm.setAutoStartup(model.getIsHighlyAvailable().getEntity());
        vm.setPriority(model.getPriority().getSelectedItem().getEntity());
        StorageDomain leaseSd = model.getLease().getSelectedItem();
        vm.setLeaseStorageDomainId(leaseSd != null ? leaseSd.getId() : null);
    }
}
