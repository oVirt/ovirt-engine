package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.CultureInfo;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DateTimeStyles;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.Date;

@SuppressWarnings("unused")
public class TimeFormatValidation implements IValidation
{
    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && !((String) value).equals("")) //$NON-NLS-1$
        {
            CultureInfo ci = CultureInfo.CurrentCulture;
            Date dtValue = new Date(0);

            RefObject<Date> tempRef_dtValue = new RefObject<Date>(dtValue);
            boolean tempVar =
                    !DateTime.TryParseExact((String) value, "t", //$NON-NLS-1$
                            ci.DateTimeFormat,
                            DateTimeStyles.None,
                            tempRef_dtValue);
            dtValue = tempRef_dtValue.argvalue;
            if (tempVar)
            {
                result.setSuccess(false);
                result.getReasons().add(ConstantsManager.getInstance()
                        .getConstants()
                        .theFieldMustContainTimeValueInvalidReason());
            }
        }

        return result;
    }
}
