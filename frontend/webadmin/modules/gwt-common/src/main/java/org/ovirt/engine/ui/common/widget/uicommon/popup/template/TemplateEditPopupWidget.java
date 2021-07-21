package org.ovirt.engine.ui.common.widget.uicommon.popup.template;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.templates.BlankTemplateModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

public class TemplateEditPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<TemplateEditPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        PopupWidgetConfigMap popupWidgetConfigMap = super.createWidgetConfiguration().
                update(foremanTab, hiddenField()).
                putOne(logicalNetworksEditorRow, hiddenField()).
                putAll(poolSpecificFields(), hiddenField()).
                putOne(detachableInstanceTypesEditor, hiddenField()).
                putOne(templateWithVersionEditor, hiddenField()).
                putAll(resourceAllocationTemplateHiddenFields(), hiddenField()).
                update(affinityTab, hiddenField());

        if (getModel() instanceof BlankTemplateModel) {
            popupWidgetConfigMap = popupWidgetConfigMap.
                putOne(dataCenterWithClusterEditor, hiddenField()).
                putOne(startRunningOnPanel, hiddenField()).
                putOne(attachCdRow, hiddenField());
        }

        return popupWidgetConfigMap;
    }

    protected List<Widget> resourceAllocationTemplateHiddenFields() {
        return Arrays.asList(
                storageAllocationPanel,
                disksAllocationPanel);
    }
}
