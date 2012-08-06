package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ExistingPoolModelBehavior extends PoolModelBehaviorBase {

    private final VM pool;

    public ExistingPoolModelBehavior(VM pool) {
        this.pool = pool;

    }

    @Override
    protected void ChangeDefualtHost() {
        super.ChangeDefualtHost();

        doChangeDefautlHost(pool.getdedicated_vm_for_vds());
    }

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        setupTemplate(pool, model);
    }

    @Override
    public void Template_SelectedItemChanged() {
        super.Template_SelectedItemChanged();
        getModel().setIsDisksAvailable(true);
        updateHostPinning(pool.getMigrationSupport());
    }

    protected void templateInited() {
        super.templateInited();

        setupWindowModelFrom(pool.getStaticData());
    }

    @Override
    protected DisplayType extractDisplayType(VmBase vmBase) {
        if (vmBase instanceof VmStatic) {
            return ((VmStatic) vmBase).getdefault_display_type();
        }

        return null;
    }

}
