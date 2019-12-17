package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.ImageHttpAccessVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(returnLevel = LogLevel.DEBUG)
public class RetrieveImageDataVDSCommand<P extends ImageHttpAccessVDSCommandParameters> extends HttpImageTaskVDSCommand<HttpGet, P> {

    public RetrieveImageDataVDSCommand(P parameters) {
        super(parameters);
    }

    protected void prepareMethod() {
        getMethod().setHeader("Range", String.format("bytes=0-%s", getParameters().getSize() - 1));
    }

    @Override
    protected void handleOkResponse(HttpResponse httpResponse) {
        processResponseHeaderValue(httpResponse, "Content-Length", getParameters().getSize().toString());

        byte[] data;
        try {
            HttpEntity entity = httpResponse.getEntity();
            data = EntityUtils.toByteArray(entity);
        } catch (Exception e) {
            throw createNetworkException(e);
        }

        if (data.length != getParameters().getSize()) {
            throwVdsErrorException(String.format("received downloaded data size is wrong (requested %d, received %d)",
                    getParameters().getSize(), data.length),
                    EngineError.GeneralException);
        }

        getVDSReturnValue().setReturnValue(data);
    }

    @Override
    protected ConfigValues getConfigValueTimeLimitForOperation() {
        return ConfigValues.RetrieveDataMaxTimeInMinutes;
    }

    @Override
    protected AsyncTaskType getCreatedTaskType() {
        return AsyncTaskType.uploadImageToStream;
    }

    @Override
    protected HttpGet concreteCreateMethod(String url) {
        return new HttpGet(url);
    }

    @Override
    protected int getSuccessCode() {
        return HttpStatus.SC_PARTIAL_CONTENT;
    }
}
