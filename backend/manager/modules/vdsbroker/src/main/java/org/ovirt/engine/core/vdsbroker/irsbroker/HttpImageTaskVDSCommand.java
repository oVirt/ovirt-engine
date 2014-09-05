package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ImageHttpAccessVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

public abstract class HttpImageTaskVDSCommand<T extends HttpMethodBase, P extends ImageHttpAccessVDSCommandParameters> extends VdsBrokerCommand<P> {
    private T method;

    public HttpImageTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        logExecution();
        prepareMethod();
        addHeaders();
        try {
            executeHttpMethod(getMethod());
        } finally {
            try {
                getMethod().releaseConnection();
            } catch (RuntimeException releaseException) {
                log.error("failed when attempting to release connection", releaseException);
            }
        }
    }

    private void addHeaders() {
        getMethod().setRequestHeader("connection", "close");
        getMethod().setRequestHeader("Storage-Pool-Id", getParameters().getStoragePoolId().toString());
        getMethod().setRequestHeader("Storage-Domain-Id", getParameters().getStorageDomainId().toString());
        getMethod().setRequestHeader("Image-Id", getParameters().getImageGroupId().toString());
        getMethod().setRequestHeader("Volume-Id", getParameters().getImageId().toString());
    }

    protected void executeHttpMethod(final T method) {
        int responseCode = -1;
        VdsManager manager = ResourceManager.getInstance().GetVdsManager(getParameters().getVdsId());
        final HttpClient httpclient = manager.getVdsProxy().getHttpClient();
        try {
            FutureTask<Integer> futureTask = new FutureTask(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return httpclient.executeMethod(method);
                }
            });
            Future<Integer> f = ThreadPoolUtil.execute(futureTask);
            if (f.get(Config.<Integer> getValue(getConfigValueTimeLimitForOperation()), TimeUnit.MINUTES) == null) {
                responseCode = futureTask.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.debug(e);
            throw createNetworkException(e);
        }

        if (responseCode == getSuccessCode()) {
            Guid createdTask =
                    Guid.createGuidFromString(processResponseHeaderValue(getMethod(), "Task-Id", null));
            getVDSReturnValue().setCreationInfo(
                    new AsyncTaskCreationInfo(createdTask, getCreatedTaskType(), getParameters()
                            .getStoragePoolId()));
            handleOkResponse();
            getVDSReturnValue().setSucceeded(true);
            return;
        }

        processResponseHeaderValue(getMethod(), "Content-type", "application/json");

        String response;
        try {
            response = getMethod().getResponseBodyAsString();
        } catch (Exception e) {
            throw createNetworkException(e);
        }

        Map<String, Object> resultMap = null;
        try {
            resultMap = new ObjectMapper().readValue(response, HashMap.class);
            status = new StatusOnlyReturnForXmlRpc(resultMap);
        } catch (Exception e) {
            throwVdsErrorException("failed to parse response " + response, VdcBllErrors.GeneralException);
        }

        proceedProxyReturnValue();
    }

    protected void handleOkResponse() {}

    protected String processResponseHeaderValue(HttpMethodBase method, String headerName, String expectedValue) {
        Header header = method.getResponseHeader(headerName);
        if (header == null) {
            throwVdsErrorException("response was missing the following header: "
                    + headerName, VdcBllErrors.GeneralException);
        }

        if (expectedValue != null && !expectedValue.equals(header.getValue())) {
            throwVdsErrorException("response header value unexpected for header: "
                    + headerName, VdcBllErrors.GeneralException);
        }

        return header.getValue();
    }

    protected T getMethod() {
        if (method == null) {
            VdsStatic vdsStatic = getAndSetVdsStatic();

            Pair<String, URL> urlInfo = XmlRpcUtils.getConnectionUrl(vdsStatic.getHostName(),
                    vdsStatic.getPort(),
                    "",
                    Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));

            method = concreteCreateMethod(urlInfo.getFirst());
        }

        return method;
    }

    private void logExecution() {
        log.info("-- executeVdsBrokerCommand: ");
        log.infoFormat("-- parameters:" + "\r\n"
                + "                spUUID={0}" + "\r\n"
                + "                sdUUID={1}" + "\r\n"
                + "                imageGUID={2}" + "\r\n"
                + "                volUUID={3}" + "\r\n"
                + "                size={4}" + "\r\n",
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getSize());
    }

    protected void throwVdsErrorException(String message, VdcBllErrors error) {
        VDSErrorException outEx = new VDSErrorException(message);
        VDSError vdsError = new VDSError();
        vdsError.setCode(error);
        vdsError.setMessage(message);
        outEx.setVdsError(vdsError);
        throw outEx;
    }

    protected void prepareMethod() {}

    protected abstract ConfigValues getConfigValueTimeLimitForOperation();

    protected abstract AsyncTaskType getCreatedTaskType();

    protected abstract T concreteCreateMethod(String url);

    protected abstract int getSuccessCode();
}
