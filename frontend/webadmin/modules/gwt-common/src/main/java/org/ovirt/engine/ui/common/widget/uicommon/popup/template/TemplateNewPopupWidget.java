package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class TemplateNewPopupWidget extends AbstractVmPopupWidget {

    public TemplateNewPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(UnitVmModel vm) {
        super.edit(vm);

        hostTab.setVisible(false);
        resourceAllocationTab.setVisible(false);
        templateEditor.setVisible(false);
        highAvailabilityTab.setVisible(true);
        customPropertiesTab.setVisible(false);
    }

}
