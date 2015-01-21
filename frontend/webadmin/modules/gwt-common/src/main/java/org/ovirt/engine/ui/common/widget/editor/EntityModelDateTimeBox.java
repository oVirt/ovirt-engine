package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * EntityModel bound DateTimeBox that uses {@link DateTimeBox}.
 */
public class EntityModelDateTimeBox extends DateTimeBox implements EditorWidget<Date, TakesValueEditor<Date>>, HasConstrainedValue<Date>{

    private TakesConstrainedValueEditor<Date> editor;
    private int tabIndex;

    private boolean enabled;
    public EntityModelDateTimeBox(boolean dateRequired, boolean timeRequired) {
        super(dateRequired, timeRequired);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return addDomHandler(handler, KeyUpEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return addDomHandler(handler, KeyDownEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return addDomHandler(handler, KeyPressEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getTabIndex() {
        return tabIndex;
    }

    @Override
    public void setAccessKey(char key) {

    }

    @Override
    public void setFocus(boolean focused) {
    }

    @Override
    public void setTabIndex(int index) {
        this.tabIndex = index;
    }

    @Override
    public TakesValueEditor<Date> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    @Override
    public void setAcceptableValues(Collection<Date> values) {
        // Keeping this mute as of now because this can take up any date
    }

}
