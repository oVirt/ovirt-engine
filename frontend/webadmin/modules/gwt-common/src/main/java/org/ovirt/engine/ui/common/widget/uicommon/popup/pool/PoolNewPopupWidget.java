package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ToStringEntityModelRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class PoolNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);

        if (object.getIsNew()) {
            object.getNumOfDesktops().setEntity(1);
        }
    }

    @Override
    protected void createNumOfDesktopEditors() {
        numOfVmsEditor = new IntegerEntityModelTextBoxEditor();
        increaseNumOfVmsEditor = new EntityModelTextBoxEditor<>(
                new ToStringEntityModelRenderer<Integer>(), text -> {
                    // forwards to the currently active editor
                    return numOfVmsEditor.asEditor().getValue();
                });
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        PopupWidgetConfigMap widgetConfiguration = super.createWidgetConfiguration().
                update(highAvailabilityTab, hiddenField()).
                update(foremanTab, hiddenField()).
                update(spiceProxyEditor, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyEnabledCheckboxWithInfoIcon, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyOverrideEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                putOne(isStatelessEditor, hiddenField()).
                putOne(isRunAndPauseEditor, hiddenField()).
                putOne(editPoolEditVmsRow, hiddenField()).
                putOne(editPoolIncreaseNumOfVmsRow, hiddenField()).
                putOne(logicalNetworksEditorRow, hiddenField()).
                putOne(editPoolEditMaxAssignedVmsPerUserRow, hiddenField()).
                putOne(baseTemplateEditor, hiddenField()).
                update(templateVersionNameEditor, hiddenField()).
                putAll(detachableWidgets(), simpleField().detachable().visibleInAdvancedModeOnly()).
                update(affinityTab, hiddenField());

        updateOrAddToWidgetConfiguration(widgetConfiguration, detachableWidgets(), PopupWidgetConfig::detachable);

        return widgetConfiguration;
    }

}
