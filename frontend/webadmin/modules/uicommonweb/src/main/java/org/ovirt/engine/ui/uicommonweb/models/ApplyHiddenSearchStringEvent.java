package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * This class is mostly copy of the {@link ApplySearchStringEvent}
 */
public class ApplyHiddenSearchStringEvent extends GwtEvent<ApplyHiddenSearchStringEvent.ApplyHiddenSearchStringHandler> {

    String hiddenSearchString;

    protected ApplyHiddenSearchStringEvent() {
        // Possibly for serialization.
    }

    public ApplyHiddenSearchStringEvent(String searchString) {
        this.hiddenSearchString = searchString;
    }

    public static void fire(HasHandlers source, String searchString) {
        ApplyHiddenSearchStringEvent eventInstance = new ApplyHiddenSearchStringEvent(searchString);
        source.fireEvent(eventInstance);
    }

    public static void fire(HasHandlers source, ApplyHiddenSearchStringEvent eventInstance) {
        source.fireEvent(eventInstance);
    }

    public interface ApplyHiddenSearchStringHandler extends EventHandler {
        void onApplyHiddenSearchString(ApplyHiddenSearchStringEvent event);
    }

    private static final Type<ApplyHiddenSearchStringHandler> TYPE = new Type<>();

    public static Type<ApplyHiddenSearchStringHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ApplyHiddenSearchStringHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ApplyHiddenSearchStringHandler handler) {
        handler.onApplyHiddenSearchString(this);
    }

    public String getHiddenSearchString() {
        return hiddenSearchString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ApplyHiddenSearchStringEvent)) {
            return false;
        }
        ApplyHiddenSearchStringEvent other = (ApplyHiddenSearchStringEvent) obj;
        return Objects.equals(hiddenSearchString, other.hiddenSearchString);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hiddenSearchString);
    }

    @Override
    public String toString() {
        return "ApplyHiddenSearchStringEvent[" //$NON-NLS-1$
                + hiddenSearchString
                + "]"; //$NON-NLS-1$
    }
}
