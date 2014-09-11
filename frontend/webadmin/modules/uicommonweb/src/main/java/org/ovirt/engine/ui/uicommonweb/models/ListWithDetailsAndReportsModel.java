package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

public abstract class ListWithDetailsAndReportsModel extends ListWithDetailsModel {
    private final Event<EventArgs> reportsAvailabilityEvent = new Event<EventArgs>(new EventDefinition("ReportsAvailabilityEvent", //$NON-NLS-1$
            ListWithDetailsAndReportsModel.class));

    public Event<EventArgs> getReportsAvailabilityEvent() {
        return reportsAvailabilityEvent;
    }

    public void updateReportsAvailability() {
        reportsAvailabilityEvent.raise(this, EventArgs.EMPTY);
    }
}
