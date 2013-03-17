package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.errors.VDSError;

public class VDSReturnValue {
    private boolean _succeeded;
    private String _exceptionString;
    private Object _returnValue;
    private RuntimeException _exceptionObject;

    public Object getReturnValue() {
        return _returnValue;
    }

    public void setReturnValue(Object value) {
        _returnValue = value;
    }

    public String getExceptionString() {
        return _exceptionString;
    }

    public void setExceptionString(String value) {
        _exceptionString = value;
    }

    public boolean getSucceeded() {
        return _succeeded;
    }

    public void setSucceeded(boolean value) {
        _succeeded = value;
    }

    public RuntimeException getExceptionObject() {
        return _exceptionObject;
    }

    public void setExceptionObject(RuntimeException value) {
        _exceptionObject = value;
    }

    private AsyncTaskCreationInfo privateCreationInfo;

    public AsyncTaskCreationInfo getCreationInfo() {
        return privateCreationInfo;
    }

    public void setCreationInfo(AsyncTaskCreationInfo value) {
        privateCreationInfo = value;
    }

    private VDSError privateVdsError;

    public VDSError getVdsError() {
        return privateVdsError;
    }

    public void setVdsError(VDSError value) {
        privateVdsError = value;
    }

    public VDSReturnValue() {
    }
}
