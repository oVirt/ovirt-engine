package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * Adapts {@link TakesValueWithChangeHandlersEditor} with {@link HasConstrainedValue} interface.
 *
 * @param <T>
 *            The type being edited.
 */
public class TakesConstrainedValueEditor<T> extends TakesValueWithChangeHandlersEditor<T> {

    public static <T> TakesConstrainedValueEditor<T> of(TakesValue<T> peer,
            HasConstrainedValue<T> peerWithConstraints,
            HasValueChangeHandlers<T> peerWithValueChangeHandlers) {
        return new TakesConstrainedValueEditor<>(peer, peerWithConstraints, peerWithValueChangeHandlers);
    }

    private final HasConstrainedValue<T> peerWithConstraints;

    protected TakesConstrainedValueEditor(TakesValue<T> peer,
            HasConstrainedValue<T> peerWithConstraints,
            HasValueChangeHandlers<T> peerWithValueChangeHandlers) {
        super(peer, peerWithValueChangeHandlers);
        this.peerWithConstraints = peerWithConstraints;
    }

    public void setAcceptableValues(Collection<T> values) {
        peerWithConstraints.setAcceptableValues(values);
    }

}
