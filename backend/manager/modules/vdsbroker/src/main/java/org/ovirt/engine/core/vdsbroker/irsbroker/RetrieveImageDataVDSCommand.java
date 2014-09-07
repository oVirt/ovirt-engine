package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ImageHttpAccessVDSCommandParameters;

public class RetrieveImageDataVDSCommand<P extends ImageHttpAccessVDSCommandParameters> extends HttpImageTaskVDSCommand<GetMethod, P> {

    public RetrieveImageDataVDSCommand(P parameters) {
        super(parameters);
    }

    protected void prepareMethod() {
        getMethod().setRequestHeader("Range", String.format("bytes=0-%s", getParameters().getSize() - 1));
    }

    @Override
    protected void handleOkResponse() {
        processResponseHeaderValue(getMethod(), "Content-Length", getParameters().getSize().toString());

        byte[] data;
        try {
            data = getMethod().getResponseBody();
        } catch (Exception e) {
            throw createNetworkException(e);
        }

        if (data.length != getParameters().getSize()) {
            throwVdsErrorException(String.format("received downloaded data size is wrong (requested %d, received %d)",
                    getParameters().getSize(), data.length),
                    VdcBllErrors.GeneralException);
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
    protected GetMethod concreteCreateMethod(String url) {
        return new GetMethod(url);
    }

    @Override
    protected int getSuccessCode() {
        return HttpStatus.SC_PARTIAL_CONTENT;
    }
}
