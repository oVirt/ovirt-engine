package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class GridTimerStateChangeEvent extends GwtEvent<GridTimerStateChangeEvent.GridTimerStateChangeEventHandler> {
    int refreshRate;

    protected GridTimerStateChangeEvent() {
        // Possibly for serialization.
    }

    public GridTimerStateChangeEvent(int newRefreshRate) {
        this.refreshRate = newRefreshRate;
    }

    public static void fire(HasHandlers source, int newRefreshRate) {
        GridTimerStateChangeEvent eventInstance = new GridTimerStateChangeEvent(newRefreshRate);
        source.fireEvent(eventInstance);
    }

    public static void fire(HasHandlers source, GridTimerStateChangeEvent eventInstance) {
        source.fireEvent(eventInstance);
    }

    public interface HasGridTimerStateChangeEventHandlers extends HasHandlers {
        HandlerRegistration addGridTimerStateChangeEventHandler(GridTimerStateChangeEventHandler handler);
    }

    public interface GridTimerStateChangeEventHandler extends EventHandler {
        public void onGridTimerStateChange(GridTimerStateChangeEvent event);
    }

    private static final Type<GridTimerStateChangeEventHandler> TYPE = new Type<>();

    public static Type<GridTimerStateChangeEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<GridTimerStateChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GridTimerStateChangeEventHandler handler) {
        handler.onGridTimerStateChange(this);
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GridTimerStateChangeEvent)) {
            return false;
        }
        GridTimerStateChangeEvent other = (GridTimerStateChangeEvent) obj;
        return refreshRate == other.refreshRate;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(refreshRate);
    }

    @Override
    public String toString() {
        return "RefreshActiveModelEvent[RefreshRate: " + refreshRate + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
