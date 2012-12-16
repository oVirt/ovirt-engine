package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * Used to represent the validation result for a "Can Do Action" validation operation, which can either return that the
 * validation succeeded, or that it failed with a specific message.<br>
 * This result can then be parsed by "Can Do Action" to decide if it should proceed or not.
 */
public final class ValidationResult {

    /**
     * A single instance for cases in which the outcome of the validation is ok.
     */
    public final static ValidationResult VALID = new ValidationResult();

    /**
     * In case the validation succeeded it is <code>null</code>, otherwise it contains the validation failure message.
     */
    private final VdcBllMessages message;

    /**
     * Default validation result is success with no message.
     * This constructor is private, it is only used to create a 'valid' result. Please use {@link ValidationResult#VALID}
     */
    private ValidationResult() {
        message = null;
    }

    /**
     * Validation result for failure with a given message.
     *
     * @param message
     *            The validation failure message.
     */
    public ValidationResult(VdcBllMessages message) {
        if(message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        this.message = message;
    }

    /**
     * @return Did the validation succeed or not?
     */
    public boolean isValid() {
        return message == null;
    }

    /**
     * @return <code>null</code> in case the validation succeeded, otherwise the validation failure message
     */
    public VdcBllMessages getMessage() {
        return message;
    }
}
