package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class VmPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmPopupWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationTemplates applicationTemplates) {
        super(constants, resources, messages, applicationTemplates);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(UnitVmModel unitVmModel) {
        super.edit(unitVmModel);

        if (unitVmModel.isVmAttachedToPool()) {
            // this just disables it, does not hides it
            specificHost.setEnabled(false);
            specificHostLabel.setStyleName(style.labelDisabled(), true);
            customPropertiesSheetEditor.setEnabled(false);
        }

    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                putAll(poolSpecificFields(), hiddenField());
    }
}
