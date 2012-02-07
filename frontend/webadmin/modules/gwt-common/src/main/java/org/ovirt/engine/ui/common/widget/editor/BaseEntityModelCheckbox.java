package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

/**
 * Base implementation of check a box editor connected to the model with the specific type T
 */
public abstract class BaseEntityModelCheckbox<T> extends Composite implements EditorWidget<T, LeafValueEditor<T>>, TakesValue<T>, HasValueChangeHandlers<T> {
    private TakesValueWithChangeHandlersEditor<T> editor;

    public BaseEntityModelCheckbox() {
        initWidget(new CheckBox());
    }

    @Override
    public TakesValueWithChangeHandlersEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesValueWithChangeHandlersEditor.of(this, this);
        }
        return editor;
    }

    public CheckBox asCheckBox() {
        return (CheckBox) getWidget();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return asCheckBox().addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return asCheckBox().addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return asCheckBox().addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return asCheckBox().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        asCheckBox().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        asCheckBox().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        asCheckBox().setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return asCheckBox().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        asCheckBox().setEnabled(enabled);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return asCheckBox().addValueChangeHandler(handler);
    }
}
