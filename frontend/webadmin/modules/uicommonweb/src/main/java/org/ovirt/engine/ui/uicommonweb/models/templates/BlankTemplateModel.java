package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;

public class BlankTemplateModel extends UnitVmModel {

    public BlankTemplateModel(VmModelBehaviorBase behavior, ListModel<?> parentModel) {
        super(behavior, parentModel);
    }

    @Override
    protected void doDisplayTypeChanged() {
        // typically the Other OS
        Integer osType = getOSType().getSelectedItem();

        displayTypeSelectedItemChanged(osType, Version.getLast());
    }
}
