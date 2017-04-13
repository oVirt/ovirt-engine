package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.List;

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
        if (getSource().isEmpty()) {
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
                    (ev, sender, args) -> {

                        CallbackContext<T> context = (CallbackContext<T>) ev.getContext();
                        AsyncIterator<T> iterator = context.getIterator();
                        AsyncIteratorPredicate<T> action1 = context.getAction();
                        T item1 = context.getItem();
                        Object value = args.getValue();

                        boolean callComplete = false;

                        if (action1.match(item1, value)) {

                            callComplete = true;
                            iterator.setStopped(true);
                        }

                        // Call complete method even when there is no match.
                        iterator.setCounter(iterator.getCounter() + 1);

                        if (!iterator.getStopped() && iterator.getCounter() == iterator.getSource().size()) {
                            callComplete = true;
                        }

                        // Call complete method.
                        if (callComplete && iterator.getComplete() != null) {
                            iterator.getComplete().run(item1, value);
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
