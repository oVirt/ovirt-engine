package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class TemplateEditPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<TemplateEditPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public TemplateEditPopupWidget(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                putOne(logicalNetworksEditorPanel, hiddenField()).
                putAll(poolSpecificFields(), hiddenField()).
                putOne(instanceTypesEditor, hiddenField()).
                putOne(templateWithVersionEditor, hiddenField()).
                putAll(resourceAllocationTemplateHiddenFields(), hiddenField());
    }

    protected List<Widget> resourceAllocationTemplateHiddenFields() {
        return Arrays.<Widget> asList(
                cpuSharesPanel,
                cpuPinningPanel,
                memAllocationPanel,
                storageAllocationPanel,
                disksAllocationPanel);
    }
}
