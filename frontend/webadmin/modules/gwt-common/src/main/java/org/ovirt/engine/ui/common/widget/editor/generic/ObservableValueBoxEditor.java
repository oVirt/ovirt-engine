package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * A {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor} that adapts to {@link com.google.gwt.event.logical.shared.HasValueChangeHandlers} interface.
 */
public class ObservableValueBoxEditor<T> extends ValueBoxEditor<T> implements HasValueChangeHandlers<T> {
    private final ValueBoxBase<T> peer;

    public ObservableValueBoxEditor(ValueBoxBase<T> peer) {
        super(peer);
        this.peer = peer;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        peer.fireEvent(event);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return peer.addValueChangeHandler(handler);
    }

}
