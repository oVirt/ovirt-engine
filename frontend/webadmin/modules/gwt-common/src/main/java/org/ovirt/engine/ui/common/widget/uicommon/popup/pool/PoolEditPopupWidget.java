package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ToStringEntityModelRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class PoolEditPopupWidget extends PoolNewPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolEditPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (object.getProgress() == null) {
                disableAllTabs();
                enableEditPoolFields(object);
            }
        });
    }

    @Override
    protected void createNumOfDesktopEditors() {
        increaseNumOfVmsEditor = new IntegerEntityModelTextBoxEditor();

        numOfVmsEditor = new EntityModelTextBoxEditor<>(new ToStringEntityModelRenderer<Integer>(), text -> {
            // forwards to the currently active editor
            return increaseNumOfVmsEditor.asEditor().getValue();
        });
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

    private void enableEditPoolFields(UnitVmModel model) {
        descriptionEditor.setEnabled(true);
        commentEditor.setEnabled(true);
        prestartedVmsEditor.setEnabled(true);

        editPrestartedVmsEditor.setEnabled(true);
        increaseNumOfVmsEditor.setEnabled(true);
        editMaxAssignedVmsPerUserEditor.setEnabled(true);

        spiceProxyEditor.setEnabled(model.getSpiceProxyEnabled().getEntity());

        templateWithVersionEditor.setEnabled(true);

        isSealedEditor.setEnabled(true);
        multiQueues.setEnabled(true);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                update(spiceProxyEditor, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyEnabledCheckboxWithInfoIcon, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyOverrideEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                update(numOfVmsEditor, hiddenField()).
                update(newPoolEditVmsRow, hiddenField()).
                update(newPoolEditMaxAssignedVmsPerUserRow, hiddenField()).
                update(editPoolEditVmsRow, simpleField()).
                update(editPoolIncreaseNumOfVmsRow, simpleField()).
                update(foremanTab, hiddenField()).
                update(editPoolEditMaxAssignedVmsPerUserRow, simpleField()).
                update(templateVersionNameEditor, hiddenField()).
                update(affinityTab, hiddenField());
    }

}
