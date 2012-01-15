package org.ovirt.engine.ui.uicommonweb.validation;

/**
 * Defines a generalized members to make a validation pass on some value.
 */
@SuppressWarnings("unused")
public interface IValidation
{
    ValidationResult Validate(Object value);
}
