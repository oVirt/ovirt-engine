package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class VmServerNewPopupWidget extends AbstractVmPopupWidget {

    public VmServerNewPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // High Availability only avail in server mode
        highAvailabilityTab.setVisible(true);

        // only avail for desktop mode
        isStatelessEditor.setVisible(false);
        numOfMonitorsEditor.setVisible(false);
        allowConsoleReconnectEditor.setVisible(false);
    }

}
