package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.TransportRunTimeException;

/**
 * This class is used for the non-blocking VDSM API. It uses Future API to fetch the response from the actual http
 * connection and computes the return value when the caller is fetching the VDSReturnValue.
 *
 */
public abstract class FutureVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> implements FutureVDSCall<VDSReturnValue> {

    private static final String TIMEOUT_MESSAGE = "Internal timeout occured";

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
     */
    @Override
    public VDSReturnValue get() {
        try {
            return get(Config.getValue(ConfigValues.vdsTimeout), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getVDSReturnValue();
        }
    }

    /**
     * Process the return value and reply back. When exceptions raises they will be logged and set a return value accordingly.
     *
     */
    @Override
    public VDSReturnValue get(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            status = new StatusOnlyReturn(httpTask.get(timeout, unit));
            checkTimeout();
            proceedProxyReturnValue();
        } catch (TimeoutException e) {
            httpTask.cancel(true);

            VDSNetworkException vdsNetworkException = new VDSNetworkException("Timeout during rpc call");
            vdsNetworkException.setVdsError(new VDSError(EngineError.VDS_NETWORK_ERROR, "Timeout during rpc call"));

            setVdsRuntimeError(vdsNetworkException);
            logTimeoutException(e, vdsNetworkException);

            throw e;
        } catch (VDSNetworkException e) {
            setVdsRuntimeErrorAndReport(e);
            if (isPolicyResetMessage(e.getVdsError().getMessage())) {
                log.info("Policy reset required for network reconfiguration");
            } else {
                log.error("Error: {}", e.getMessage());
                log.debug("Exception", e);
            }
        } catch (TransportRunTimeException e) {
            handleTransportRunTimeException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
        return getVDSReturnValue();
    }

    protected void logTimeoutException(TimeoutException e, VDSNetworkException ex) {
        log.error("Timeout waiting for VDSM response: {}", e.getMessage());
        log.debug("Exception", e);
    }

    private void handleGenericException(Exception e) {
        log.error("Error: {}", e.getMessage());
        log.debug("Exception", e);
        setVdsRuntimeErrorAndReport(e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
    }

    protected void handleTransportRunTimeException(TransportRunTimeException e) {
        handleGenericException(e);
    }

    private void checkTimeout() throws TimeoutException {
        String message = getReturnStatus().message;
        if (TIMEOUT_MESSAGE.equals(message)) {
            throw new TimeoutException(message);
        }
    }

    @Override
    protected abstract void executeVdsBrokerCommand();

}
