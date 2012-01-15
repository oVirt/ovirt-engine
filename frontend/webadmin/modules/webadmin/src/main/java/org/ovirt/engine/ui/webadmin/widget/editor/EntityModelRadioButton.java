package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * This class extends Composite instead of RadioButton because RadioButton is a Boolean type editor.
 */
public class EntityModelRadioButton extends Composite implements EditorWidget<Object, LeafValueEditor<Object>>, TakesValue<Object>, HasValueChangeHandlers<Object> {

    private TakesValueWithChangeHandlersEditor<Object> editor;

    public EntityModelRadioButton(String group) {
        initWidget(new RadioButton(group));
    }

    @Override
    public TakesValueWithChangeHandlersEditor<Object> asEditor() {
        if (editor == null) {
            editor = TakesValueWithChangeHandlersEditor.of(this, this);
        }
        return editor;
    }

    public RadioButton asRadioButton() {
        return (RadioButton) getWidget();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return asRadioButton().addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return asRadioButton().addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return asRadioButton().addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return asRadioButton().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        asRadioButton().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        asRadioButton().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        asRadioButton().setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return asRadioButton().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        asRadioButton().setEnabled(enabled);
    }

    @Override
    public Object getValue() {
        return asRadioButton().getValue();
    }

    @Override
    public void setValue(Object value) {
        asRadioButton().setValue((Boolean) value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return asRadioButton().addValueChangeHandler(handler);
    }

    public void setLabel(String label) {
        asRadioButton().setText(label);
    }

}
