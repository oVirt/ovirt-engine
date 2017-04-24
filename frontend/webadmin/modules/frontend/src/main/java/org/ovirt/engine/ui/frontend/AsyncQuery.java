package org.ovirt.engine.ui.frontend;

public class AsyncQuery<T> {

    /**
     * Null object singleton that represents an empty (no-op) query callback.
     */
    private static final AsyncCallback EMPTY_CALLBACK = returnValue -> {};

    private final Object model;
    private final AsyncCallback<T> asyncCallback;
    public Converter<T, ?> converterCallback = null;
    private boolean handleFailure;

    public AsyncQuery() {
        this.model = null;
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

    public boolean isHandleFailure() {
        return handleFailure;
    }

    public void setHandleFailure(boolean handleFailure) {
        this.handleFailure = handleFailure;
    }

    public Object getModel() {
        return model;
    }

    public AsyncCallback<T> getAsyncCallback() {
        return asyncCallback;
    }

    public Converter<T, ?> getConverter() {
        return converterCallback;
    }

}
