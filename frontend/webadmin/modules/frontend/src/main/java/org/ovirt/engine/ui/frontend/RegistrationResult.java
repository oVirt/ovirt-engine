package org.ovirt.engine.ui.frontend;

import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

public final class RegistrationResult {
    /**
     Raised once when a first result retrievement occurs.

    */
    private Event<EventArgs> privateRetrievedEvent;

    public Event<EventArgs> getRetrievedEvent() {
        return privateRetrievedEvent;
    }

    private void setRetrievedEvent(Event<EventArgs> value) {
        privateRetrievedEvent = value;
    }

    public static final EventDefinition RetrievedEventDefinition;

    private Guid privateId = Guid.Empty;

    public Guid getId() {
        return privateId;
    }

    private void setId(Guid value) {
        privateId = value;
    }

    private ObservableCollection<Queryable> privateData;

    public ObservableCollection<Queryable> getData() {
        return privateData;
    }

    private void setData(ObservableCollection<Queryable> value) {
        privateData = value;
    }

    private int privateRetrievementCount;

    public int getRetrievementCount() {
        return privateRetrievementCount;
    }

    public void setRetrievementCount(int value) {
        privateRetrievementCount = value;
    }

    static {
        RetrievedEventDefinition = new EventDefinition("RetrievedEvent", RegistrationResult.class); //$NON-NLS-1$
    }

    public RegistrationResult(Guid id, ObservableCollection<Queryable> data) {
        setRetrievedEvent(new Event<>(RetrievedEventDefinition));

        setId(id);
        setData(data);
    }

}
