package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

/**
 * Query return value class, If inheriting from this class add logic to QueriesCommandBase class.
 */
public class VdcQueryReturnValue implements Serializable {
    private static final long serialVersionUID = -7737597005584540780L;

    private boolean _succeeded;
    private String _exceptionString;
    private Object returnValue;

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object value) {
        returnValue = value;
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

    public VdcQueryReturnValue() {
    }
}
