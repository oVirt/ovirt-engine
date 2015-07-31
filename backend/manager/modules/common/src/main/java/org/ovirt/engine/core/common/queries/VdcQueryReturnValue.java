package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

/**
 * Query return value class, If inheriting from this class add logic to QueriesCommandBase class.
 */
public class VdcQueryReturnValue implements Serializable {
    private static final long serialVersionUID = -7737597005584540781L;

    private boolean succeeded;
    private String exceptionString;
    private String exceptionCode;
    private Object returnValue;

    @SuppressWarnings("unchecked")
    public <T> T getReturnValue() {
        return (T) returnValue;
    }

    public void setReturnValue(Object value) {
        returnValue = value;
    }

    /**
     * Get the exception code, which can be an enum value defined in AppErrors.
     */
    public String getExceptionString() {
        return exceptionString;
    }

    /**
     * Set the exception code, which can be an enum value defined in AppErrors.
     */
    public void setExceptionString(String value) {
        exceptionString = value;
    }

    /**
     * Get the exception message, which is a human-readable, localized error message (see AppErrors).
     */
    public String getExceptionMessage() {
        return exceptionCode;
    }

    /**
     * Set the exception message, which is a human-readable, localized error message (see AppErrors).
     */
    public void setExceptionMessage(String value) {
        exceptionCode = value;
    }

    public boolean getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean value) {
        succeeded = value;
    }
}
