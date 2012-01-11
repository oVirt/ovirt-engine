package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;

public class Enlistment {

    public static EventDefinition DoneEventDefinition;
    private Event doneEvent;

    protected Event getDoneEvent() {
        return doneEvent;
    }

    private Object context;

    public Object getContext() {
        return context;
    }

    static {

        DoneEventDefinition = new EventDefinition("Done", PreparingEnlistment.class);
    }

    public Enlistment(Object context) {

        doneEvent = new Event(DoneEventDefinition);
        this.context = context;
    }

    public void Done() {
        getDoneEvent().raise(this, EventArgs.Empty);
    }
}
