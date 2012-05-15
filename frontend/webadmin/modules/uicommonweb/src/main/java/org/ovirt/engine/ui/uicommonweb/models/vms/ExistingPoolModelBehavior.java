package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ExistingPoolModelBehavior extends PoolModelBehaviorBase {

    private final VM vm;

    public ExistingPoolModelBehavior(VM pool) {
        this.vm = pool;

    }

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        setupTemplate(vm, model);
    }

}
