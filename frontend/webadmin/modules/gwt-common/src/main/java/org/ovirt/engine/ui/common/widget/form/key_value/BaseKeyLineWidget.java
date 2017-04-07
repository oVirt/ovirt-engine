package org.ovirt.engine.ui.common.widget.form.key_value;

import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;

public abstract class BaseKeyLineWidget<M extends KeyLineModel> extends Composite implements HasValueChangeHandlers<M>, HasEditorDriver<M>, HasEnabled {

    @UiField
    Row panel;

    @UiField
    @Path(value = "keys.selectedItem")
    ListModelListBoxEditor<String> keyField;

    protected abstract void init();

    public void doEdit(final M object) {
        updateKeyTitle(object);
        object.getKeys().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            ValueChangeEvent.fire(BaseKeyLineWidget.this, object);
            updateKeyTitle(object);
        });
    }

    protected void hideLabels() {
        keyField.hideLabel();
    }

    public void setUsePatternFly(boolean use) {
        keyField.setUsePatternFly(use);
    }

    /**
     * set dropdown with selected key tooltip.
     */
    protected void updateKeyTitle(M object) {
        String selectedKey = object.getKeys().getSelectedItem();
        if (selectedKey != null) {
            keyField.setWidgetTooltip(selectedKey);
        }
    }

    @Override
    public boolean isEnabled() {
        return keyField.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        keyField.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<M> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
