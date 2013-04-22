package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;

public class AsyncIteratorCallback {

    public static EventDefinition NotifyEventDefinition;

    private Event notifyEvent;

    /**
     * Notifies iterator about item retrieval completion.
     */
    public Event getNotifyEvent() {
        return notifyEvent;
    }

    private void setNotifyEvent(Event value) {
        notifyEvent = value;
    }

    AsyncQuery asyncQuery;

    /**
     * Returns instance of AsyncQuery type that can be used in AsyncDataProvider.
     */
    public AsyncQuery getAsyncQuery() {
        return asyncQuery;
    }

    private void setAsyncQuery(AsyncQuery value) {
        asyncQuery = value;
    }

    static {
        NotifyEventDefinition = new EventDefinition("Notify", AsyncIteratorCallback.class); //$NON-NLS-1$
    }

    public AsyncIteratorCallback(String frontendContext) {

        setNotifyEvent(new Event(NotifyEventDefinition));

        // Set a stub method calling notify event on AsyncQuery complete.
        setAsyncQuery(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        AsyncIteratorCallback callback = (AsyncIteratorCallback) target;
                        Event notifyEvent = callback.getNotifyEvent();

                        notifyEvent.raise(this, new ValueEventArgs(returnValue));
                    }
                },
                frontendContext));
    }
}
