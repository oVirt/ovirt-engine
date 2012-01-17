package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * Used to represent the validation result for a "Can Do Action" validation operation, which can either return that the
 * validation succeeded, or that it failed with a specific message.<br>
 * This result can then be parsed by "Can Do Action" to decide if it should proceed or not.
 */
public class ValidationResult {

    /**
     * Indicates if the validation succeeded or failed.
     */
    private boolean valid;

    /**
     * In case the validation succeeded it is <code>null</code>, otherwise it contains the validation failure message.
     */
    private VdcBllMessages message;

    /**
     * Default validation result is success with no message.
     */
    public ValidationResult() {
        valid = true;
    }

    /**
     * Validation result for failure with a given message.
     *
     * @param message
     *            The validation failure message.
     */
    public ValidationResult(VdcBllMessages message) {
        this.message = message;
    }

    /**
     * @return Did the validation succeed or not?
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return <code>null</code> in case the validation succeeded, otherwise the validation failure message
     */
    public VdcBllMessages getMessage() {
        return message;
    }
}
