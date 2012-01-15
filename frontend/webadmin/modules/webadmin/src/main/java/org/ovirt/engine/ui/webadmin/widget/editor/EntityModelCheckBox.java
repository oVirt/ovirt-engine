package org.ovirt.engine.ui.webadmin.widget.editor;

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
 * This class extends Composite instead of CheckBox because CheckBox is a Boolean type editor.
 */
public class EntityModelCheckBox extends Composite implements EditorWidget<Object, LeafValueEditor<Object>>, TakesValue<Object>, HasValueChangeHandlers<Object> {

    private TakesValueWithChangeHandlersEditor<Object> editor;

    public EntityModelCheckBox() {
        initWidget(new CheckBox());
    }

    @Override
    public TakesValueWithChangeHandlersEditor<Object> asEditor() {
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

    @Override
    public Object getValue() {
        return asCheckBox().getValue();
    }

    @Override
    public void setValue(Object value) {
        asCheckBox().setValue((Boolean) value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return asCheckBox().addValueChangeHandler(handler);
    }

}
