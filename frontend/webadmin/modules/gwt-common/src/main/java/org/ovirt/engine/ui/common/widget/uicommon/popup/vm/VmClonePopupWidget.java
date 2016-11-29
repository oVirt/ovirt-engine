package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;

import com.google.gwt.core.client.GWT;

public class VmClonePopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmClonePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                update(foremanTab, hiddenField()).
                putAll(poolSpecificFields(), hiddenField()).
                putOne(logicalNetworksEditorRow, hiddenField()).
                update(consoleTab, simpleField().visibleInAdvancedModeOnly()).
                putOne(baseTemplateEditor, hiddenField());
    }
}
