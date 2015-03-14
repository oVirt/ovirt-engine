package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;

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
 * ListModel bound DaysOfMonthSelector that extends {@link DaysOfMonthSelector}.
 */
public class ListModelDaysOfMonthSelector extends DaysOfMonthSelector implements EditorWidget<String, TakesValueEditor<String>>, HasConstrainedValue<String> {

    private TakesConstrainedValueEditor<String> editor;

    private int tabIndex;

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
    public TakesValueEditor<String> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    @Override
    public void setAcceptableValues(Collection<String> values) {
        // Keeping this mute as of now because the values set from the bound ListModel's setItems don't have any
        // significance here and hence shouldn't be done.
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Keeping this mute as no use of this can thought as of now.

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
