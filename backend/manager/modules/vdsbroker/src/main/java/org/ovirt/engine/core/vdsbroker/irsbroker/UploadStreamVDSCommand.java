package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;

public class UploadStreamVDSCommand<P extends UploadStreamVDSCommandParameters> extends HttpImageTaskVDSCommand<HttpPut, P> {

    public UploadStreamVDSCommand(P parameters) {
        super(parameters);
    }

    protected void prepareMethod() {
        InputStreamEntity inputStreamRequestEntity;
        if (getParameters().getSize() != null) {
            inputStreamRequestEntity =
                    new InputStreamEntity(getParameters().getInputStream(),
                            getParameters().getSize());
        } else {
            inputStreamRequestEntity = new InputStreamEntity(getParameters().getInputStream());
        }

        getMethod().setEntity(inputStreamRequestEntity);
        getMethod().setHeader("Content-Type", "application/octet-stream");
        if (getParameters().getSize() != null) {
            getMethod().setHeader("Content-Length", getParameters().getSize().toString());
        }
    }

    @Override
    protected AsyncTaskType getCreatedTaskType() {
        return AsyncTaskType.downloadImageFromStream;
    }

    @Override
    protected HttpPut concreteCreateMethod(String url) {
        return new HttpPut(url);
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
