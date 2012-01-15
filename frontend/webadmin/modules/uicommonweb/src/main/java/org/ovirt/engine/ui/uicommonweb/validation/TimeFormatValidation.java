package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.CultureInfo;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DateTimeStyles;
import org.ovirt.engine.core.compat.RefObject;

@SuppressWarnings("unused")
public class TimeFormatValidation implements IValidation
{
    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && !((String) value).equals(""))
        {
            CultureInfo ci = CultureInfo.CurrentCulture;
            java.util.Date dtValue = new java.util.Date(0);

            RefObject<java.util.Date> tempRef_dtValue = new RefObject<java.util.Date>(dtValue);
            boolean tempVar =
                    !DateTime.TryParseExact((String) value,
                            "t",
                            ci.DateTimeFormat,
                            DateTimeStyles.None,
                            tempRef_dtValue);
            dtValue = tempRef_dtValue.argvalue;
            if (tempVar)
            {
                result.setSuccess(false);
                result.getReasons().add("The field must contain a time value");
            }
        }

        return result;
    }
}
