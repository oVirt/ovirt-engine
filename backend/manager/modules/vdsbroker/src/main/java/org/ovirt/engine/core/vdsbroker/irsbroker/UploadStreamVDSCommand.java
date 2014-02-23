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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSErrorException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsServerWrapper;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

public class UploadStreamVDSCommand<P extends UploadStreamVDSCommandParameters> extends VdsBrokerCommand<P> {

    private static final Log log = LogFactory.getLog(UploadStreamVDSCommand.class);

    public UploadStreamVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- executeVdsBrokerCommand: ");
        log.infoFormat("-- upload parameters:" + "\r\n"
                + "                dstSpUUID={0}" + "\r\n"
                + "                dstSdUUID={1}" + "\r\n"
                + "                dstImageGUID={2}" + "\r\n"
                + "                dstVolUUID={3}" + "\r\n",
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());
        VdsManager manager = ResourceManager.getInstance().GetVdsManager(getParameters().getVdsId());
        final HttpClient httpclient = ((VdsServerWrapper) manager.getVdsProxy()).getHttpClient();
        VdsStatic vdsStatic =
                DbFacade.getInstance().getVdsStaticDao().get(manager.getVdsId());

        Pair<String, URL> urlInfo = XmlRpcUtils.getConnectionUrl(vdsStatic.getHostName(),
                vdsStatic.getPort(),
                "",
                Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));

        final PutMethod putMethod =
                new PutMethod(urlInfo.getFirst());
        try {
            InputStreamRequestEntity inputStreamRequestEntity = null;
            if (getParameters().getStreamLength() != null) {
                inputStreamRequestEntity =
                        new InputStreamRequestEntity(getParameters().getInputStream(),
                                getParameters().getStreamLength());
            } else {
                inputStreamRequestEntity = new InputStreamRequestEntity((getParameters().getInputStream()));
            }

            putMethod.setRequestEntity(inputStreamRequestEntity);
            putMethod.setRequestHeader("Content-Type", "application/octet-stream");
            if (getParameters().getStreamLength() != null) {
                putMethod.setRequestHeader("Content-Length", getParameters().getStreamLength().toString());
            }

            putMethod.setRequestHeader("connection", "close");
            putMethod.setRequestHeader("Storage-Pool-Id", getParameters().getStoragePoolId().toString());
            putMethod.setRequestHeader("Storage-Domain-Id", getParameters().getStorageDomainId().toString());
            putMethod.setRequestHeader("Image-Id", getParameters().getImageGroupId().toString());
            putMethod.setRequestHeader("Volume-Id", getParameters().getImageId().toString());

            int responseCode = -1;

            try {
                FutureTask<Integer> futureTask = new FutureTask(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return httpclient.executeMethod(putMethod);
                    }
                });
                Future<Integer> f = ThreadPoolUtil.execute(futureTask);
                if (f.get(Config.<Integer> getValue(ConfigValues.UploadFileMaxTimeInMinutes), TimeUnit.MINUTES) == null) {
                    responseCode = futureTask.get();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.debug(e);
                throw createNetworkException(e);
            }

            if (responseCode != HttpStatus.SC_OK) {
                throwVdsErrorException("upload failed with response code " + responseCode, VdcBllErrors.UPLOAD_FAILURE);
            }

            processResponseHeaderValue(putMethod, "Content-type", "application/json");

            String response;
            try {
                response = putMethod.getResponseBodyAsString();
            } catch (Exception e) {
                throw createNetworkException(e);
            }

            Map<String, Object> resultMap = null;
            try {
                resultMap = new ObjectMapper().readValue(response, HashMap.class);
                status = new StatusOnlyReturnForXmlRpc(resultMap);
            } catch (Exception e){
                throwVdsErrorException("failed to parse response " + response, VdcBllErrors.GeneralException);
            }

            proceedProxyReturnValue();

            String createdTaskId = new OneUuidReturnForXmlRpc(resultMap).mUuid;

            Guid createdTask = Guid.createGuidFromString(createdTaskId);

            getVDSReturnValue().setCreationInfo(
                    new AsyncTaskCreationInfo(createdTask, AsyncTaskType.downloadImageFromStream, getParameters()
                            .getStoragePoolId()));

            getVDSReturnValue().setSucceeded(true);
        } finally {
            try {
                putMethod.releaseConnection();
            } catch (RuntimeException releaseException) {
                log.error("failed when attempting to release connection", releaseException);
            }
        }
    }

    private void throwVdsErrorException(String message, VdcBllErrors error) {
        VDSErrorException outEx = new VDSErrorException(message);
        VDSError tempVar = new VDSError();
        tempVar.setCode(error);
        tempVar.setMessage(message);
        outEx.setVdsError(tempVar);
        throw outEx;
    }

    private String processResponseHeaderValue(HttpMethodBase method, String headerName, String expectedValue) {
        Header header = method.getResponseHeader(headerName);
        if (header == null) {
            throwVdsErrorException("UploadStreamVDSCommand - response was missing the following header: "
                    + headerName, VdcBllErrors.GeneralException);
        }

        if (expectedValue != null && !expectedValue.equals(header.getValue())) {
            throwVdsErrorException("UploadStreamVDSCommand - response header value unexpected for header: "
                    + headerName, VdcBllErrors.GeneralException);
        }

        return header.getValue();
    }
}
