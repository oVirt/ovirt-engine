package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicompat.UriValidator;

@SuppressWarnings("unused")
public class HostAddressValidation implements IValidation
{
    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        String val = (String) value;
        if (StringHelper.isNullOrEmpty(val) || !UriValidator.IsValid(val))
        {
            result.setSuccess(false);
            result.getReasons().add("Address is not a valid host name or IP address.");
        }
        return result;
    }
}
