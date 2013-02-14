package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Collection;

/**
 * Validator that returns true if the given object is contained
 * in a given collection, and false otherwise
 *
 * Note: this validator is abstract as it doesn't return any
 * reason when the result is false
 */
public abstract class NotInCollectionValidation implements IValidation {

    private Collection<?> collection;

    public NotInCollectionValidation(Collection<?> collection) {
        this.collection = collection;
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();
        result.setSuccess(!collection.contains(value));
        return result;
    }
}
