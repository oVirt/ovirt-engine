package org.ovirt.engine.core.searchbackend;

/**
 * Interface for field validators.
 */
@FunctionalInterface
public interface ValueValidationFunction {
    /**
     * Checks if the field us valid.
     * @param field the name of the field
     * @param value the field value
     * @return  true if the field is valid, false otherwise
     */
    boolean isValid(String field, String value);
}
