package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * A {@link ValueBoxEditor} that adapts to {@link HasValueChangeHandlers} interface.
 */
public class ObservableValueBoxEditor extends ValueBoxEditor<Object> implements HasValueChangeHandlers<Object> {

    public static ObservableValueBoxEditor of(ValueBoxBase<Object> valueBox) {
        return new ObservableValueBoxEditor(valueBox);
    }

    private final ValueBoxBase<Object> peer;

    protected ObservableValueBoxEditor(ValueBoxBase<Object> peer) {
        super(peer);
        this.peer = peer;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        peer.fireEvent(event);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Object> handler) {
        return peer.addValueChangeHandler(handler);
    }

}
