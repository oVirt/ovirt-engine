package org.ovirt.engine.ui.uicommonweb.validation;

/**
 * Defines a generalized members to make a validation pass on some value.
 */
public interface IValidation {
    ValidationResult validate(Object value);
}
