package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

/**
 * @param <E> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.E}
 * @param <T> {@link org.ovirt.engine.ui.uicommonweb.models.SearchableListModel.T}
 */
public abstract class SearchableListWithReportsModel<E, T> extends SearchableListModel<E, T> {

    private final Event<EventArgs> reportsAvailabilityEvent = new Event<>(new EventDefinition("ReportsAvailabilityEvent", //$NON-NLS-1$
            SearchableListWithReportsModel.class));

    public Event<EventArgs> getReportsAvailabilityEvent() {
        return reportsAvailabilityEvent;
    }

    public void updateReportsAvailability() {
        reportsAvailabilityEvent.raise(this, EventArgs.EMPTY);
    }
}
