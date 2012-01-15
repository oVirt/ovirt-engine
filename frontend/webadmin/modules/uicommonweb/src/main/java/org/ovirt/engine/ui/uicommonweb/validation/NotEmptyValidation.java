package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.StringHelper;

@SuppressWarnings("unused")
public class NotEmptyValidation implements IValidation
{
    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value == null || (value instanceof String && StringHelper.isNullOrEmpty((String) value)))
        {
            result.setSuccess(false);
            result.getReasons().add("This field can't be empty.");
        }

        return result;
    }
}
