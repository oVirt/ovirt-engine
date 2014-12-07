package org.ovirt.engine.ui.frontend;

import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public class AsyncQuery {

    /**
     * Null object singleton that represents an empty (no-op) query callback.
     */
    private static final INewAsyncCallback EMPTY_CALLBACK = new INewAsyncCallback() {
        @Override
        public void onSuccess(Object model, Object returnValue) {
            // Empty
        }
    };

    public Object model;
    public INewAsyncCallback asyncCallback;
    public IAsyncConverter converterCallback = null;
    private boolean handleFailure;
    public VdcQueryReturnValue originalReturnValue;
    public Object[] data;

    public AsyncQuery() {
        this.asyncCallback = EMPTY_CALLBACK;
    }

    public AsyncQuery(INewAsyncCallback asyncCallback) {
        this(null, asyncCallback);
    }

    public AsyncQuery(Object target, INewAsyncCallback asyncCallback) {
        setModel(target);
        this.asyncCallback = asyncCallback;
    }

    public AsyncQuery(Object target, INewAsyncCallback asyncCallback, boolean handleFailure) {
        setModel(target);
        this.asyncCallback = asyncCallback;
        this.handleFailure = handleFailure;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public VdcQueryReturnValue getOriginalReturnValue() {
        return originalReturnValue;
    }

    public void setOriginalReturnValue(VdcQueryReturnValue originalReturnValue) {
        this.originalReturnValue = originalReturnValue;
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

    public void setModel(Object model) {
        this.model = model;
    }

    public INewAsyncCallback getDel() {
        return asyncCallback;
    }

    public void setDel(INewAsyncCallback asyncCallback) {
        this.asyncCallback = asyncCallback;
    }

    public IAsyncConverter getConverter() {
        return converterCallback;
    }

}
