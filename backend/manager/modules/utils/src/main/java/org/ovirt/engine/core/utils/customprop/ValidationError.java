package org.ovirt.engine.core.utils.customprop;

/**
 * Class describing errors appear during custom properties validation
 */
public class ValidationError {
    /**
     * Reason of error
     */
    private final ValidationFailureReason reason;

    /**
     * Property name on which the error appeared
     */
    private final String keyName;

    /**
     * Creates instance with specified reason and property name
     *
     * @param reason
     *            reason of error
     * @param keyName
     *            property name
     */
    public ValidationError(ValidationFailureReason reason, String keyName) {
        this.reason = reason;
        this.keyName = keyName;
    }

    /**
     * Returns reason of error
     */
    public ValidationFailureReason getReason() {
        return reason;
    }

    /**
     * Returns property name on which the error appeared
     */
    public String getKeyName() {
        return keyName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ValidationError) {
            ValidationError otherError = (ValidationError) other;
            return keyName.equals(otherError.getKeyName()) && reason == otherError.getReason();
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((keyName == null) ? 0 : keyName.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        return result;
    }

    /**
     * Returns {@code true}, if no error appeared, otherwise {@code false}
     */
    public boolean isOK() {
        return reason == ValidationFailureReason.NO_ERROR;
    }
}
