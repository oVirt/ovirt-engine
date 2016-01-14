package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.StringHelper;

public class AsyncTaskStatus implements Serializable {
    private static final long serialVersionUID = -7569773307084259828L;

    private AsyncTaskStatusEnum status;
    private AsyncTaskResultEnum result;
    private RuntimeException exception;
    private String message;

    public AsyncTaskStatus() {
        this(AsyncTaskStatusEnum.unknown);
    }

    public AsyncTaskStatus(AsyncTaskStatusEnum status) {
        setStatus(status);
        setResult(AsyncTaskResultEnum.success);
        setMessage("");
    }

    public AsyncTaskStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AsyncTaskStatusEnum status) {
        this.status = status;
    }

    public AsyncTaskResultEnum getResult() {
        return result;
    }

    public void setResult(AsyncTaskResultEnum result) {
        this.result = result;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException value) {
        this.exception = value;
    }

    public String getMessage() {
        if (getTaskIsRunning() || getTaskEndedSuccessfully()) {
            // No message is relevant:
            return "";
        }
        return !StringHelper.isNullOrEmpty(message) ? message : getException() != null
                && !StringHelper.isNullOrEmpty(getException().getMessage()) ? getException().getMessage()
                : "Asynchronous Task unknown error";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getTaskIsRunning() {
        return getStatus() != AsyncTaskStatusEnum.finished && getStatus() != AsyncTaskStatusEnum.unknown;
    }

    public boolean getTaskIsInUnusualState() {
        return getStatus() == AsyncTaskStatusEnum.unknown || getStatus() == AsyncTaskStatusEnum.aborting;
    }

    public boolean getTaskEndedSuccessfully() {
        return getStatus() == AsyncTaskStatusEnum.finished && getResult() == AsyncTaskResultEnum.success
                && getException() == null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("status", getStatus())
                .append("result", getResult())
                .append("exception", getException())
                .append("message", getMessage())
                .build();
    }
}
