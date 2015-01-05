package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;

import com.google.gwt.core.client.GWT;

public class VmClonePopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmClonePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmClonePopupWidget(CommonApplicationConstants constants,
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
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                putAll(poolSpecificFields(), hiddenField()).
                putOne(logicalNetworksEditorPanel, hiddenField()).
                update(consoleTab, simpleField().visibleInAdvancedModeOnly()).
                putOne(baseTemplateEditor, hiddenField());
    }
}
