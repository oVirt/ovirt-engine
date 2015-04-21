package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.errors.VDSError;

public class VDSReturnValue {

    protected Object returnValue;
    private boolean succeeded;
    private String exceptionString;
    private RuntimeException exceptionObject;
    private AsyncTaskCreationInfo creationInfo;
    private VDSError vdsError;
    private boolean canTryOnDifferentVds;

    public VDSReturnValue() {
        exceptionString = "";
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object value) {
        returnValue = value;
    }

    public String getExceptionString() {
        return exceptionString;
    }

    public void setExceptionString(String value) {
        exceptionString = value;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean value) {
        succeeded = value;
    }

    public RuntimeException getExceptionObject() {
        return exceptionObject;
    }

    public void setExceptionObject(RuntimeException value) {
        exceptionObject = value;
    }

    public AsyncTaskCreationInfo getCreationInfo() {
        return creationInfo;
    }

    public void setCreationInfo(AsyncTaskCreationInfo value) {
        creationInfo = value;
    }

    public VDSError getVdsError() {
        return vdsError;
    }

    public void setVdsError(VDSError value) {
        vdsError = value;
    }

    public boolean isCanTryOnDifferentVds() {
        return canTryOnDifferentVds;
    }

    public void setCanTryOnDifferentVds(boolean isVdsSpecificError) {
        this.canTryOnDifferentVds = isVdsSpecificError;
    }
}
