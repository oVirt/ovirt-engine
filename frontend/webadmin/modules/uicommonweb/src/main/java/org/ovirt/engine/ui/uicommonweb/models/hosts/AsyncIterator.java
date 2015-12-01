package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class AsyncIterator<T> {

    private List<T> source;

    private List<T> getSource() {
        return source;
    }

    private AsyncIteratorComplete<T> complete;

    public AsyncIteratorComplete<T> getComplete() {
        return complete;
    }

    public void setComplete(AsyncIteratorComplete<T> value) {
        complete = value;
    }

    private boolean stopped;

    public boolean getStopped() {
        return stopped;
    }

    public void setStopped(boolean value) {
        stopped = value;
    }

    private int counter;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int value) {
        counter = value;
    }

    public AsyncIterator(List<T> source) {
        this.source = source;
    }

    public void iterate(AsyncIteratorFunc<T> func, AsyncIteratorPredicate<T> action) {

        setCounter(0);

        // Call complete method in case source is an empty list.
        if (Linq.count(getSource()) == 0) {
            if (getComplete() != null) {
                getComplete().run(null, null);
            }

            return;
        }

        for (T item : getSource()) {

            if (getStopped()) {
                break;
            }

            AsyncIteratorCallback<T> callback = new AsyncIteratorCallback<>();

            callback.getNotifyEvent().addListener(
                    new IEventListener<ValueEventArgs<T>>() {
                        @Override
                        public void eventRaised(Event<? extends ValueEventArgs<T>> ev, Object sender, ValueEventArgs<T> args) {

                            CallbackContext<T> context = (CallbackContext<T>) ev.getContext();
                            AsyncIterator<T> iterator = context.getIterator();
                            AsyncIteratorPredicate<T> action = context.getAction();
                            T item = context.getItem();
                            Object value = args.getValue();

                            boolean callComplete = false;

                            if (action.match(item, value)) {

                                callComplete = true;
                                iterator.setStopped(true);
                            }

                            // Call complete method even when there is no match.
                            iterator.setCounter(iterator.getCounter() + 1);

                            if (!iterator.getStopped() && iterator.getCounter() == Linq.count(iterator.getSource())) {
                                callComplete = true;
                            }

                            // Call complete method.
                            if (callComplete && iterator.getComplete() != null) {
                                iterator.getComplete().run(item, value);
                            }
                        }
                    },
                    new CallbackContext<>(this, item, action));

            func.run(item, callback);
        }
    }

    private class CallbackContext<T> {

        private AsyncIterator<T> iterator;

        public AsyncIterator<T> getIterator() {
            return iterator;
        }

        private AsyncIteratorPredicate<T> action;

        public AsyncIteratorPredicate<T> getAction() {
            return action;
        }

        private T item;

        public T getItem() {
            return item;
        }

        private Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        private CallbackContext(AsyncIterator<T> iterator, T item, AsyncIteratorPredicate<T> action) {
            this.iterator = iterator;
            this.item = item;
            this.action = action;
        }
    }
}
