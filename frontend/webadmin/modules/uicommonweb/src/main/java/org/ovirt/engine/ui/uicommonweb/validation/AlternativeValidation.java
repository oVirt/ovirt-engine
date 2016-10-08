package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;
import java.util.List;

public class AlternativeValidation implements IValidation {

    private final String failReason;
    private final List<IValidation> validations;

    public AlternativeValidation(String failReason, IValidation... validations) {
        this(failReason, Arrays.asList(validations));
    }

    public AlternativeValidation(String failReason, List<IValidation> validations) {
        if (failReason == null || validations == null) {
            throw new IllegalArgumentException();
        }

        this.failReason = failReason;
        this.validations = validations;
    }

    @Override
    public ValidationResult validate(Object value) {
        if (validations.isEmpty()) {
            return ValidationResult.ok();
        }

        for (IValidation validation : validations) {
            if (validation.validate(value).getSuccess()) {
                return ValidationResult.ok();
            }
        }

        return ValidationResult.fail(failReason);
    }
}
