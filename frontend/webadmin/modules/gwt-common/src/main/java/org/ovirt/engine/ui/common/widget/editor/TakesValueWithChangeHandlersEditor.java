package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;

/**
 * Adapts {@link TakesValueEditor} to {@link HasValueChangeHandlers} interface.
 *
 * @param <T>
 *            The type being edited.
 */
public class TakesValueWithChangeHandlersEditor<T> extends TakesValueEditor<T> implements HasValueChangeHandlers<T> {

    public static <T> TakesValueWithChangeHandlersEditor<T> of(TakesValue<T> peer,
            HasValueChangeHandlers<T> peerWithValueChangeHandlers) {
        return new TakesValueWithChangeHandlersEditor<>(peer, peerWithValueChangeHandlers);
    }

    private final HasValueChangeHandlers<T> peerWithValueChangeHandlers;

    protected TakesValueWithChangeHandlersEditor(TakesValue<T> peer,
            HasValueChangeHandlers<T> peerWithValueChangeHandlers) {
        super(peer);
        this.peerWithValueChangeHandlers = peerWithValueChangeHandlers;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        peerWithValueChangeHandlers.fireEvent(event);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return peerWithValueChangeHandlers.addValueChangeHandler(handler);
    }

}
