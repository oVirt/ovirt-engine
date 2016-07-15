package org.ovirt.engine.ui.frontend;

public class AsyncQuery<T> {

    /**
     * Null object singleton that represents an empty (no-op) query callback.
     */
    private static final AsyncCallback EMPTY_CALLBACK = new AsyncCallback() {
        @Override
        public void onSuccess(Object returnValue) {
            // Empty
        }
    };

    public Object model;
    public AsyncCallback<T> asyncCallback;
    public Converter<T> converterCallback = null;
    private boolean handleFailure;

    public AsyncQuery() {
        this.asyncCallback = EMPTY_CALLBACK;
    }

    public AsyncQuery(AsyncCallback<T> asyncCallback) {
        this(null, asyncCallback);
    }

    public AsyncQuery(AsyncCallback<T> asyncCallback, boolean handleFailure) {
        this(null, asyncCallback, handleFailure);
    }

    protected AsyncQuery(Object target, AsyncCallback<T> asyncCallback) {
        this.model = target;
        this.asyncCallback = asyncCallback;
    }

    protected AsyncQuery(Object target, AsyncCallback<T> asyncCallback, boolean handleFailure) {
        this.model = target;
        this.asyncCallback = asyncCallback;
        this.handleFailure = handleFailure;
    }

    public AsyncQuery<T> handleFailure() {
        setHandleFailure(true);
        return this;
    }

    public AsyncQuery<T> withConverter(Converter<T> converter) {
        this.converterCallback = converter;
        return this;
    }

    public boolean isHandleFailure() {
        return handleFailure;
    }

    public void setHandleFailure(boolean handleFailure) {
        this.handleFailure = handleFailure;
    }

    public Object getModel() {
        return model;
    }

    public AsyncCallback<T> getDel() {
        return asyncCallback;
    }

    public void setDel(AsyncCallback<T> asyncCallback) {
        this.asyncCallback = asyncCallback;
    }

    public Converter<T> getConverter() {
        return converterCallback;
    }

}
