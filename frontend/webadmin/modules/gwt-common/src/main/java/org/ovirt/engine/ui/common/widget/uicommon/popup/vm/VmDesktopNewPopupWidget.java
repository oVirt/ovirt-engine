package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class VmDesktopNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmDesktopNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmDesktopNewPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources, CommonApplicationMessages messages) {
        super(constants, resources, messages);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);

        if (object.isVmAttachedToPool()) {
            specificHost.setEnabled(false);
            specificHostLabel.setStyleName(style.labelDisabled(), true);
            customPropertiesSheetEditor.setEnabled(false);
        }
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // High Availability only avail in server mode
        highAvailabilityTab.setVisible(false);

        // only avail for desktop mode
        isStatelessEditor.setVisible(true);
        numOfMonitorsEditor.setVisible(true);
        allowConsoleReconnectEditor.setVisible(true);
        expander.setVisible(true);
    }
}
