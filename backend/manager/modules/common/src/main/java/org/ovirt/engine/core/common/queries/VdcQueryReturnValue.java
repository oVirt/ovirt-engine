package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

/**
 * Query return value class, If inheriting from this class add logic to QueriesCommandBase class.
 */
public class VdcQueryReturnValue implements Serializable {
    private static final long serialVersionUID = -7737597005584540780L;

    private boolean succeeded;
    private String exceptionString;
    private Object returnValue;

    public VdcQueryReturnValue() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getReturnValue() {
        return (T) returnValue;
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
}
