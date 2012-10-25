package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class TemplateNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<TemplateNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public TemplateNewPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources, CommonApplicationMessages messages, CommonApplicationTemplates applicationTemplates) {
        super(constants, resources, messages, applicationTemplates);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(UnitVmModel vm) {
        super.edit(vm);

        hostTab.setVisible(false);
        resourceAllocationTab.setVisible(false);
        templateEditor.setVisible(false);
        highAvailabilityTab.setVisible(true);
        customPropertiesTab.setVisible(true);
    }

}
