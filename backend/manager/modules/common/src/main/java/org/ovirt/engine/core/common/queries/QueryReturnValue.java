package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.common.HasCorrelationId;

/**
 * Query return value class.
 *
 * <p>
 * Final on purpose, to avoid the need of implementing GWT custom field serializer
 * for each subclass. Instead of subclassing, consider using the {@link #returnValue}
 * object itself to carry any additional data.
 * </p>
 */
public final class QueryReturnValue implements Serializable, HasCorrelationId {
    private static final long serialVersionUID = -8111910149841490393L;

    private boolean succeeded;
    private String exceptionString;
    private String exceptionCode;
    private Object returnValue;
    private String correlationId;

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

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setCorrelationId(String value) {
        correlationId = value;
    }
}
