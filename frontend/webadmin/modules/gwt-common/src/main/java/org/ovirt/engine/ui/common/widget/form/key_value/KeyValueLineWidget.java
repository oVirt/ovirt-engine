package org.ovirt.engine.ui.common.widget.form.key_value;

import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueLineWidget extends Composite implements HasValueChangeHandlers<KeyValueLineModel>, HasEditorDriver<KeyValueLineModel>, HasEnabled {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueLineWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends UiCommonEditorDriver<KeyValueLineModel, KeyValueLineWidget> {
    }

    private boolean enabled = true;

    @UiField
    @Ignore
    Row panel;

    @UiField
    @Path(value = "keys.selectedItem")
    ListModelListBoxEditor<String> keyField;

    @UiField
    @Path(value = "value.entity")
    StringEntityModelTextBoxEditor valueField;

    @UiField
    @Path(value = "values.selectedItem")
    ListModelListBoxEditor<String> valuesField;

    private final Driver driver = GWT.create(Driver.class);

    KeyValueLineWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        hideLabels();
    }

    private void hideLabels() {
        keyField.hideLabel();
        valueField.hideLabel();
        valuesField.hideLabel();
    }

    public void setUsePatternFly(boolean use) {
        keyField.setUsePatternFly(use);
        valueField.setUsePatternFly(use);
        valuesField.setUsePatternFly(use);
    }

    @Override
    public void edit(final KeyValueLineModel object) {
        updateKeyTitle(object);
        object.getKeys().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            ValueChangeEvent.fire(KeyValueLineWidget.this, object);
            updateKeyTitle(object);
        });
        driver.edit(object);
    }

    /**
     * set dropdown with selected key tooltip.
     */
    private void updateKeyTitle(KeyValueLineModel object) {
        String selectedKey = object.getKeys().getSelectedItem();
        if (selectedKey != null) {
            keyField.setWidgetTooltip(selectedKey);
        }
    }

    @Override
    public KeyValueLineModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        keyField.setEnabled(enabled);
        valueField.setEnabled(enabled);
        valuesField.setEnabled(enabled);
        this.enabled = enabled;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<KeyValueLineModel> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
