package org.ovirt.engine.core.notifier.utils.sender;

/**
 * Describes the result of send attempt.
 */
public class EventSenderResult {
    /**
     * The reason of the result (e.g. cause of sent failure, sent only on third attempt,...)
     */
    private String reason = "";

    /**
     * the status of the notify action: true for a successful dispatch or false for a failure. If failed additional data
     * could be acquired by {@link #getReason()}
     */
    private boolean isSent;

    /**
     * Describes the reason of the result
     * @return a plan text describes the reason of the result
     */
    public String getReason() {
        return reason;
    }

    /**
     * A setter of the reason for the result
     * @param reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the status of the notify action: true for a successful dispatch or <br>
     * false for a failure. If failed additional data could be acquired by {@link #getReason()}
     * @return {@code true} for a successful dispatch of {@code false} for a failure
     */
    public boolean isSent() {
        return isSent;
    }

    /**
     * Set the status of the result
     * @param isSent
     */
    public void setSent(boolean isSent) {
        this.isSent = isSent;
    }
}
