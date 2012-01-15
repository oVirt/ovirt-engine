package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.MailAddress;

@SuppressWarnings("unused")
public class EmailValidation implements IValidation
{
    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        try
        {
            new MailAddress((String) value);
        } catch (RuntimeException e)
        {
            result.setSuccess(false);
            result.getReasons().add("Invalid E-Mail address");
        }

        return result;
    }
}
