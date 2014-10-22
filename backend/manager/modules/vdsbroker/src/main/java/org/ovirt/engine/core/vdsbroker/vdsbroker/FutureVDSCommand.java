package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

/**
 * This class is used for the non-blocking VDSM API. It uses Future API to fetch the response from the actual http
 * connection and computes the return value when the the caller is fetching the VDSReturnValue.
 *
 */
public abstract class FutureVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> implements FutureVDSCall<VDSReturnValue> {

    public FutureVDSCommand(P parameters) {
        super(parameters);
    }

    protected Future<Map<String, Object>> httpTask;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return httpTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return httpTask.isCancelled();
    }

    @Override
    public boolean isDone() {
        return httpTask.isDone();
    }


    /**
     * get the return value and wait for the reply with default host connection timeout. We need to assure that this
     * thread is released after a timeout so the blocking call for get is overridden here and delegated to the get(long
     * timeout, TimeUnit unit)
     *
     * @return {@link VDSReturnValue}
     */
    @Override
    public VDSReturnValue get() {
        try {
            return get(Config.<Integer> getValue(ConfigValues.vdsTimeout), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getVDSReturnValue();
        }
    }

    /**
     * Process the return value and reply back. When exceptions raises they will be logged and set a return value accordingly.
     *
     * @param timeout
     * @param unit
     * @return VDSReturnValue
     * @throws TimeoutException
     */
    @Override
    public VDSReturnValue get(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            status = new StatusOnlyReturnForXmlRpc(httpTask.get(timeout, unit));
            proceedProxyReturnValue();
        } catch (TimeoutException e) {
            httpTask.cancel(true);
            VDSNetworkException ex = new VDSNetworkException("Timeout during xml-rpc call");
            ex.setVdsError(new VDSError(VdcBllErrors.VDS_NETWORK_ERROR, "Timeout during xml-rpc call"));
            setVdsRuntimeError(ex);
            log.error("Timeout waiting for VDSM response: {}", e.getMessage());
            log.debug("Exception", e);
            throw e;
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            log.error("Exception", e);
            setVdsRuntimeError(e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }
        return getVDSReturnValue();
    }

    @Override
    abstract protected void executeVdsBrokerCommand();

}
