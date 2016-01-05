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
    protected void initGraphicsConsoles() {
        // typically the Other OS
        Integer osType = getOSType().getSelectedItem();

        if (osType == null) {
            return;
        }

        initGraphicsConsoles(osType, Version.getLast());
    }

    @Override
    public Version getCompatibilityVersion() {
        return Version.getLast();
    }
}
