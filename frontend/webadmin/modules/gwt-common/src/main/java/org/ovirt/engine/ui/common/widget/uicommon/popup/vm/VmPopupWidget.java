package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

public class VmPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmPopupWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationTemplates applicationTemplates,
            EventBus eventBus) {
        super(constants, resources, messages, applicationTemplates, eventBus);
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
        }

    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        PopupWidgetConfigMap widgetConfiguration = super.createWidgetConfiguration().
                putAll(poolSpecificFields(), hiddenField()).
                putOne(baseTemplateEditor, hiddenField()).
                update(templateVersionNameEditor, hiddenField()).
                update(instanceImagesEditor, simpleField());

        updateOrAddToWidgetConfiguration(widgetConfiguration, detachableWidgets(), UpdateToDetachable.INSTANCE);
        updateOrAddToWidgetConfiguration(widgetConfiguration, adminOnlyWidgets(), UpdateToAdminOnly.INSTANCE);

        return widgetConfiguration;
    }
}
