package org.ovirt.engine.ui.uicommonweb.models;

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

    private static final Type<GridTimerStateChangeEventHandler> TYPE = new Type<GridTimerStateChangeEventHandler>();

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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GridTimerStateChangeEvent other = (GridTimerStateChangeEvent) obj;
        if (refreshRate != other.refreshRate)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 23;
        hashCode = (hashCode * 37) + Integer.valueOf(refreshRate).hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "RefreshActiveModelEvent[RefreshRate: " + refreshRate + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
