package org.ovirt.engine.ui.uicompat;


public class PreparingEnlistment extends Enlistment {

    public static final EventDefinition preparedEventDefinition;
    private Event<EventArgs> preparedEvent;

    protected Event<EventArgs> getPreparedEvent() {
        return preparedEvent;
    }

    public static final EventDefinition rollbackEventDefinition;
    private Event<EventArgs> rollbackEvent;

    protected Event<EventArgs> getRollbackEvent() {
        return rollbackEvent;
    }


    static {

        preparedEventDefinition = new EventDefinition("Prepared", PreparingEnlistment.class); //$NON-NLS-1$
        rollbackEventDefinition = new EventDefinition("Rollback", PreparingEnlistment.class); //$NON-NLS-1$
    }

    public PreparingEnlistment(Object context) {
        super(context);

        preparedEvent = new Event<>(preparedEventDefinition);
        rollbackEvent = new Event<>(rollbackEventDefinition);
    }

    public void prepared() {
        getPreparedEvent().raise(this, EventArgs.EMPTY);
    }

    public void forceRollback() {
        getRollbackEvent().raise(this, EventArgs.EMPTY);
    }
}
