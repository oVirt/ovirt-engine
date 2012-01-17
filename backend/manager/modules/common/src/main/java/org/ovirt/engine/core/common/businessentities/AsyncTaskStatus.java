package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AsyncTaskStatus")
public class AsyncTaskStatus implements Serializable {
    private static final long serialVersionUID = -7569773307084259828L;

    public AsyncTaskStatus() {
    }

    public AsyncTaskStatus(AsyncTaskStatusEnum status) {
        setStatus(status);
    }

    @XmlElement(name = "Status")
    private AsyncTaskStatusEnum privateStatus = AsyncTaskStatusEnum.forValue(0);

    public AsyncTaskStatusEnum getStatus() {
        return privateStatus;
    }

    public void setStatus(AsyncTaskStatusEnum value) {
        privateStatus = value;
    }

    @XmlElement(name = "Result")
    private AsyncTaskResultEnum privateResult = AsyncTaskResultEnum.forValue(0);

    public AsyncTaskResultEnum getResult() {
        return privateResult;
    }

    public void setResult(AsyncTaskResultEnum value) {
        privateResult = value;
    }

    // @XmlElement
    private RuntimeException privateException;

    public RuntimeException getException() {
        return privateException;
    }

    public void setException(RuntimeException value) {
        privateException = value;
    }

    @XmlElement(name = "Message")
    private String _message = "";

    public String getMessage() {
        if (getTaskIsRunning() || getTaskEndedSuccessfully()) {
            // No message is relevant:
            return "";
        }
        return (!StringHelper.isNullOrEmpty(_message)) ? (_message) : ((getException() != null && !StringHelper
                .isNullOrEmpty(getException().getMessage())) ? (getException().getMessage())
                : ("Asynchronous Task unknown error"));
    }

    public void setMessage(String value) {
        _message = value;
    }

    public boolean getTaskIsRunning() {
        return getStatus() != AsyncTaskStatusEnum.finished && getStatus() != AsyncTaskStatusEnum.unknown;
    }

    public boolean getTaskIsInUnusualState() {
        return (getStatus() == AsyncTaskStatusEnum.unknown || getStatus() == AsyncTaskStatusEnum.aborting);
    }

    public boolean getTaskEndedSuccessfully() {
        return getStatus() == AsyncTaskStatusEnum.finished && getResult() == AsyncTaskResultEnum.success
                && getException() == null;
    }

    @Override
    public String toString() {
        return "(status = " + getStatus() + ", result = " + getResult() + ", exception = " + getException() + ", msg = " +getMessage() + ")";
    }
}
