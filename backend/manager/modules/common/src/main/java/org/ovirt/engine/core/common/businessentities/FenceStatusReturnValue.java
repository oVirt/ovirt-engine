package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.StringHelper;

public class FenceStatusReturnValue implements Serializable {
    private static final long serialVersionUID = 8070963676213797507L;
    // indicates that operation was skipped because Host is already in requested state.
    public static final String SKIPPED = "skipped";
    public static final String INITIATED = "initiated";
    public FenceStatusReturnValue(String status, String message) {
        _status = status;
        _message = message;
    }

    private String _status;

    public String getStatus() {
        return _status;
    }

    private String _message;

    public String getMessage() {
        return ("done".equalsIgnoreCase(_message)) ? "" : _message;
    }

    public boolean getIsSucceeded() {
        return (StringHelper.isNullOrEmpty(getMessage()));
    }

    public boolean getIsSkipped() {
        return SKIPPED.equalsIgnoreCase(_status);
    }

    public boolean getIsInitiated() {
        return INITIATED.equalsIgnoreCase(_status);
    }

    @Override
    public String toString() {
        final String TEST_SUCCEEDED = "Test Succeeded, ";
        final String TEST_FAILED = "Test Failed, ";

        return getIsSucceeded() ? TEST_SUCCEEDED + getStatus() : TEST_FAILED + getMessage();
    }

    public FenceStatusReturnValue() {
    }
}
