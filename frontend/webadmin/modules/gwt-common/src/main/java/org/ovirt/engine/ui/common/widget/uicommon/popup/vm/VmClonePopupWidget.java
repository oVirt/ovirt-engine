package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class VmClonePopupWidget extends AbstractVmPopupWidget {

    public VmClonePopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        boolean isDesktop = vm.getVmType().equals(VmType.Desktop);

        // High Availability only available in server mode
        highAvailabilityTab.setVisible(!isDesktop);

        // only available for desktop mode
        isStatelessEditor.setVisible(isDesktop);
        numOfMonitorsEditor.setVisible(isDesktop);
    }

}
