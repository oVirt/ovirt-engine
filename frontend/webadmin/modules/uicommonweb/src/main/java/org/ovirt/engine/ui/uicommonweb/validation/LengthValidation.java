package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.StringFormat;

@SuppressWarnings("unused")
public class LengthValidation implements IValidation
{
    private int privateMaxLength;

    public int getMaxLength()
    {
        return privateMaxLength;
    }

    public void setMaxLength(int value)
    {
        privateMaxLength = value;
    }

    public LengthValidation()
    {
        setMaxLength(Integer.MAX_VALUE);
    }

    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && ((String) value).length() > getMaxLength())
        {
            result.setSuccess(false);
            result.getReasons().add(StringFormat.format("Field content must not exceed %1$s characters.",
                    getMaxLength()));
        }

        return result;
    }
}
