package org.ovirt.engine.core.common.businessentities.pm;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * Result of action performed on single fence agent
 */
public class FenceOperationResult implements Serializable {
    /**
     * Operation result
     */
    private Status status;

    /**
     * Power status
     */
    private PowerStatus powerStatus;

    /**
     * Message received from fence agent
     */
    private String message;

    /**
     * Creates instance with status {@code Status.ERROR}, power status {@code PowerStatus.UNKNOWN}
     */
    public FenceOperationResult() {
        this(Status.ERROR, PowerStatus.UNKNOWN);
    }

    /**
     * Creates instance with specified status, power status {@code PowerStatus.UNKNOWN} and {@code null}
     * error message
     */
    public FenceOperationResult(Status status) {
        this(status, PowerStatus.UNKNOWN);
    }

    /**
     * Creates instance with specified status, powerStatus
     */
    public FenceOperationResult(Status status, PowerStatus powerStatus) {
        this(status, powerStatus, "");
    }

    /**
     * Creates instance with specified status, powerStatus and message
     */
    public FenceOperationResult(Status status, PowerStatus powerStatus, String message) {
        this.status = status;
        this.powerStatus = powerStatus;
        this.message = message;
    }

    /**
     * Creates instance using requested action and result of RPC call
     */
    public FenceOperationResult(
            FenceActionType action,
            int code,
            String message,
            String power,
            String operationStatus) {

        Status st = code == 0 ? Status.SUCCESS : Status.ERROR;
        powerStatus = PowerStatus.forValue(power);
        this.message = message;

        if (action == FenceActionType.STATUS && powerStatus == PowerStatus.UNKNOWN) {
            st = Status.ERROR;
        }

        if (action != FenceActionType.STATUS && "skipped".equalsIgnoreCase(operationStatus)) {
            /*
             * TODO:
             *     This needs to be fixed when we move the check "is host already in desired state"
             *     for on/off action from engine to VDSM
             */
            st = Status.SKIPPED_DUE_TO_POLICY;
        }

        status = st;
    }

    public Status getStatus() {
        return status;
    }

    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    public String getMessage() {
        // VSDM returns 'Done' by default for fencing operations. This is not useful and appending
        // it to log messages results in a confusing message. So if the message is 'Done', it's
        // better to ignore it (return empty string). If it's anything different - return it as is.
        // (See: https://bugzilla.redhat.com/1203741.)
        if (message.equals("Done")) {
            return "";
        } else {
            return message;
        }
    }

    public enum Status {
        /**
         * Operation finished successfully
         */
        SUCCESS,

        /**
         * Operation finished with error
         */
        ERROR,

        /**
         * Operation was skipped, because host is already in required power status
         */
        SKIPPED_ALREADY_IN_STATUS,

        /**
         * Operation was skipped due to fencing policy
         */
        SKIPPED_DUE_TO_POLICY
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("status", getStatus())
                .append("powerStatus", getPowerStatus())
                .append("message", getMessage())
                .build();
    }
}
