package org.ovirt.engine.ui.uicompat;


public class PreparingEnlistment extends Enlistment {

    public static EventDefinition PreparedEventDefinition;
    private Event preparedEvent;

    protected Event getPreparedEvent() {
        return preparedEvent;
    }

    public static EventDefinition RollbackEventDefinition;
    private Event rollbackEvent;

    protected Event getRollbackEvent() {
        return rollbackEvent;
    }


    static {

        PreparedEventDefinition = new EventDefinition("Prepared", PreparingEnlistment.class); //$NON-NLS-1$
        RollbackEventDefinition = new EventDefinition("Rollback", PreparingEnlistment.class); //$NON-NLS-1$
    }

    public PreparingEnlistment(Object context) {
        super(context);

        preparedEvent = new Event(PreparedEventDefinition);
        rollbackEvent = new Event(RollbackEventDefinition);
    }

    public void Prepared() {
        getPreparedEvent().raise(this, EventArgs.Empty);
    }

    public void ForceRollback() {
        getRollbackEvent().raise(this, EventArgs.Empty);
    }
}
