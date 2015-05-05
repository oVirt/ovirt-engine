package org.ovirt.engine.core.common.vdscommands;


/**
 * <code>VDSAsyncReturnValue</code> represents asynchronous result.
 *
 * It gives a possibility to check whether the response arrived from
 * vdsm and uses {@link AsyncCallback} to process it and return a proper
 * business object.
 */
public abstract class VDSAsyncReturnValue extends VDSReturnValue {

    private AsyncCallback callback;

    public VDSAsyncReturnValue() {
    }

    public VDSAsyncReturnValue(AsyncCallback callback, Object value) {
        this.callback = callback;
        returnValue = value;
    }

    /**
     * @return <code>true</code> if response arrived or xmlrpc used and
     *         <code>false</code> otherwise.
     */
    public abstract boolean isRequestCompleted();

    @Override
    public Object getReturnValue() {
        return callback.process(returnValue, this);
    }

}
