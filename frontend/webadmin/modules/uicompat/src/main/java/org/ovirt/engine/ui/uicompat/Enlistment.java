package org.ovirt.engine.ui.uicompat;


public class Enlistment {

    public static final EventDefinition doneEventDefinition;
    private Event<EventArgs> doneEvent;

    protected Event<EventArgs> getDoneEvent() {
        return doneEvent;
    }

    private Object context;

    public Object getContext() {
        return context;
    }

    static {

        doneEventDefinition = new EventDefinition("Done", PreparingEnlistment.class); //$NON-NLS-1$
    }

    public Enlistment(Object context) {

        doneEvent = new Event<>(doneEventDefinition);
        this.context = context;
    }

    public void done() {
        getDoneEvent().raise(this, EventArgs.EMPTY);
    }
}
