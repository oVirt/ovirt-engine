package org.ovirt.engine.ui.common.widget.uicommon;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.NetworkFilterParameterModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class NetworkFilterParameterEditor extends AbstractModelBoundPopupWidget<NetworkFilterParameterModel>
        implements HasValueChangeHandlers<NetworkFilterParameterModel>, HasEnabled {

    public final Driver driver = GWT.create(Driver.class);

    interface Driver extends UiCommonEditorDriver<NetworkFilterParameterModel, NetworkFilterParameterEditor> {
    }

    interface WidgetUiBinder extends UiBinder<Widget, NetworkFilterParameterEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NetworkFilterParameterEditor> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;
    @UiField
    @Path("value.entity")
    @WithElementId("value")
    StringEntityModelTextBoxEditor valueEditor;


    public NetworkFilterParameterEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void edit(NetworkFilterParameterModel model) {
        driver.edit(model);
        model.getName()
                .getEntityChangedEvent()
                .addListener((ev, sender, args) -> ValueChangeEvent.fire(NetworkFilterParameterEditor.this, model));
    }

    @Override
    public NetworkFilterParameterModel flush() {
        return driver.flush();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<NetworkFilterParameterModel> valueChangeHandler) {
        return addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return nameEditor.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        nameEditor.setEnabled(enabled);
        valueEditor.setEnabled(enabled);
    }
}
