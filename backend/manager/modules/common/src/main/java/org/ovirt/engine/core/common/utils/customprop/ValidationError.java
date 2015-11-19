package org.ovirt.engine.core.common.utils.customprop;

import java.util.Objects;

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValidationError)) {
            return false;
        }
        ValidationError other = (ValidationError) obj;
        return Objects.equals(keyName, other.keyName)
                && reason == other.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                keyName,
                reason
        );
    }

    /**
     * Returns {@code true}, if no error appeared, otherwise {@code false}
     */
    public boolean isOK() {
        return reason == ValidationFailureReason.NO_ERROR;
    }
}
