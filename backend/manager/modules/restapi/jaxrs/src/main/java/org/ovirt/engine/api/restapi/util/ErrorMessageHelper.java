package org.ovirt.engine.api.restapi.util;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.ErrorType;

public class ErrorMessageHelper {

    public static Status getErrorStatus(String error) {
        try {
            ErrorType errorType = EngineMessage.getErrorType(error);
            return getStatus(errorType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the most severe error status out of the given errors. This methos exists to handle cases where multiple
     * Validate messages were returned from Backend, which is quite rare.
     */
    public static Response.Status getErrorStatus(List<String> errors) {
        Status status = Status.BAD_REQUEST; // default
        if (errors != null) {
            List<Status> statuses = getStatuses(errors);
            if (statuses.contains(Status.BAD_REQUEST)) {
                // stay with the default - BAD_REQUEST.
            } else if (statuses.contains(Status.CONFLICT)) {
                status = Status.CONFLICT;
            } else if (statuses.contains(Status.INTERNAL_SERVER_ERROR)) {
                status = Status.INTERNAL_SERVER_ERROR;
            } else if (statuses.contains(Status.FORBIDDEN)) {
                status = Status.FORBIDDEN;
            }
        }
        return status;
    }

    private static List<Status> getStatuses(List<String> errors) {
        List<Status> statuses = new LinkedList<>();
        for (String error : errors) {
            Status status = getErrorStatus(error);
            if (status != null) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    private static Status getStatus(ErrorType errorType) {
        switch (errorType) {
        case BAD_PARAMETERS:
            return Status.BAD_REQUEST;
        case CONFLICT:
            return Status.CONFLICT;
        case CONSTRAINT_VIOLATION:
            return Status.BAD_REQUEST;
        case DATA_CORRUPTION:
            return Status.INTERNAL_SERVER_ERROR;
        case INTERNAL_ERROR:
            return Status.INTERNAL_SERVER_ERROR;
        case NO_AUTHENTICATION:
            return Status.UNAUTHORIZED;
        case NO_PERMISSION:
            return Status.FORBIDDEN;
        case NOT_SUPPORTED:
            return Status.BAD_REQUEST;
        case INCOMPATIBLE_VERSION:
            return Status.BAD_REQUEST;
        case ATTESTATION_SERVER_ERROR:
            return Status.BAD_REQUEST;
        default:
            return Status.BAD_REQUEST;
        }
    }

}
