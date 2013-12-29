package org.ovirt.engine.ui.uicompat;


public class PreparingEnlistment extends Enlistment {

    public static EventDefinition preparedEventDefinition;
    private Event preparedEvent;

    protected Event getPreparedEvent() {
        return preparedEvent;
    }

    public static EventDefinition rollbackEventDefinition;
    private Event rollbackEvent;

    protected Event getRollbackEvent() {
        return rollbackEvent;
    }


    static {

        preparedEventDefinition = new EventDefinition("Prepared", PreparingEnlistment.class); //$NON-NLS-1$
        rollbackEventDefinition = new EventDefinition("Rollback", PreparingEnlistment.class); //$NON-NLS-1$
    }

    public PreparingEnlistment(Object context) {
        super(context);

        preparedEvent = new Event(preparedEventDefinition);
        rollbackEvent = new Event(rollbackEventDefinition);
    }

    public void prepared() {
        getPreparedEvent().raise(this, EventArgs.EMPTY);
    }

    public void forceRollback() {
        getRollbackEvent().raise(this, EventArgs.EMPTY);
    }
}
