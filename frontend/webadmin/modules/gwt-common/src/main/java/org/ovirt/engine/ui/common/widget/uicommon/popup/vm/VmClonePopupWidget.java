package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class VmClonePopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmClonePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmClonePopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources, CommonApplicationMessages messages, CommonApplicationTemplates applicationTemplates) {
        super(constants, resources, messages, applicationTemplates);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
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
