package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;

public class UploadStreamVDSCommand<P extends UploadStreamVDSCommandParameters> extends HttpImageTaskVDSCommand<PutMethod, P> {

    public UploadStreamVDSCommand(P parameters) {
        super(parameters);
    }

    protected void prepareMethod() {
        InputStreamRequestEntity inputStreamRequestEntity;
        if (getParameters().getSize() != null) {
            inputStreamRequestEntity =
                    new InputStreamRequestEntity(getParameters().getInputStream(),
                            getParameters().getSize());
        } else {
            inputStreamRequestEntity = new InputStreamRequestEntity(getParameters().getInputStream());
        }

        getMethod().setRequestEntity(inputStreamRequestEntity);
        getMethod().setRequestHeader("Content-Type", "application/octet-stream");
        if (getParameters().getSize() != null) {
            getMethod().setRequestHeader("Content-Length", getParameters().getSize().toString());
        }
    }

    @Override
    protected AsyncTaskType getCreatedTaskType() {
        return AsyncTaskType.downloadImageFromStream;
    }

    @Override
    protected PutMethod concreteCreateMethod(String url) {
        return new PutMethod(url);
    }

    @Override
    protected ConfigValues getConfigValueTimeLimitForOperation() {
        return ConfigValues.UploadFileMaxTimeInMinutes;
    }

    @Override
    protected int getSuccessCode() {
        return HttpStatus.SC_OK;
    }
}
