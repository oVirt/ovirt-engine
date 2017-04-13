package org.ovirt.engine.ui.uicompat;


import java.util.ArrayList;
import java.util.List;

public class ReversibleFlow {

    List<IEnlistmentNotification> notifications;
    int prepareIndex;
    int commitIndex;
    int rollbackIndex;
    Object context;

    public static final EventDefinition completeEventDefinition;
    private Event<EventArgs> completeEvent;

    public Event<EventArgs> getCompleteEvent() {
        return completeEvent;
    }

    static {

        completeEventDefinition = new EventDefinition("Complete", ReversibleFlow.class); //$NON-NLS-1$
    }


    public ReversibleFlow() {

        completeEvent = new Event<>(completeEventDefinition);
        notifications = new ArrayList<>();
    }

    public void enlist(IEnlistmentNotification notification) {

        // Add notifier to the list.
        notifications.add(notification);
    }

    /**
     * Note, the current implementation is not thread safe (good enough for UI)
     */
    public void run() {

        // Begin processing notifications.
        if (notifications.size() > 0) {
            prepareIndex = 0;
            rollbackIndex = notifications.size();
            prepare();
        }
    }

    public void run(Object context) {

        this.context = context;
        run();
    }

    private void prepare() {

        PreparingEnlistment enlistment = new PreparingEnlistment(context);

        enlistment.getPreparedEvent().addListener((ev, sender, args) -> preparedHandler());

        enlistment.getRollbackEvent().addListener((ev, sender, args) -> rollbackHandler());

        enlistment.getDoneEvent().addListener((ev, sender, args) -> doneOnPrepareHandler());

        IEnlistmentNotification notification = notifications.get(prepareIndex);
        notification.prepare(enlistment);
    }

    private void commit() {

        Enlistment enlistment = new Enlistment(context);

        enlistment.getDoneEvent().addListener((ev, sender, args) -> doneOnCommitHandler());

        IEnlistmentNotification notification = notifications.get(commitIndex);
        notification.commit(enlistment);
    }

    private void rollback() {

        Enlistment enlistment = new Enlistment(context);

        enlistment.getDoneEvent().addListener((ev, sender, args) -> doneOnRollbackHandler());

        IEnlistmentNotification notification = notifications.get(rollbackIndex);
        notification.rollback(enlistment);
    }

    private void preparedHandler() {

        if (prepareIndex == notifications.size() - 1) {

            // Move to the next (commit) stage.
            commitIndex = 0;
            commit();

        } else {
            // Process next notification.
            prepareIndex++;
            prepare();
        }
    }

    private void rollbackHandler() {

        if (rollbackIndex == 0) {
            complete();

        } else {
            // Process next notification.
            rollbackIndex--;
            rollback();
        }
    }

    private void doneOnPrepareHandler() {
        complete();
    }

    private void doneOnRollbackHandler() {
        rollbackHandler();
    }

    private void doneOnCommitHandler() {

        if (commitIndex == notifications.size() - 1) {
            complete();

        } else {
            // Process next notification.
            commitIndex++;
            commit();
        }
    }

    private void complete() {
        getCompleteEvent().raise(this, EventArgs.EMPTY);
    }
}
