package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ImageHttpAccessVDSCommandParameters;

public class RetrieveImageDataVDSCommand<P extends ImageHttpAccessVDSCommandParameters> extends HttpImageTaskVDSCommand<GetMethod, P> {

    public RetrieveImageDataVDSCommand(P parameters) {
        super(parameters);
    }

    protected void prepareMethod() {
        getMethod().setRequestHeader("Size", getParameters().getSize().toString());
    }

    @Override
    protected void handleOkResponse() {
        byte[] data;
        try {
            data = getMethod().getResponseBody();
        } catch (Exception e) {
            throw createNetworkException(e);
        }

        getVDSReturnValue().setReturnValue(data);
    }

    @Override
    protected ConfigValues getConfigValueTimeLimitForOperation() {
        return ConfigValues.RetrieveDataMaxTimeInMinutes;
    }

    @Override
    protected AsyncTaskType getCreatedTaskType() {
        return AsyncTaskType.uploadImageFromStream;
    }

    @Override
    protected GetMethod concreteCreateMethod(String url) {
        return new GetMethod(url);
    }

    @Override
    protected int getSuccessCode() {
        return HttpStatus.SC_OK;
    }
}
