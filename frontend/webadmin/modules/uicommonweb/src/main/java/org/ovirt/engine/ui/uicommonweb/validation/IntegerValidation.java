package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;

@SuppressWarnings("unused")
public class IntegerValidation implements IValidation
{
    private int privateMaximum;

    public int getMaximum()
    {
        return privateMaximum;
    }

    public void setMaximum(int value)
    {
        privateMaximum = value;
    }

    private int privateMinimum;

    public int getMinimum()
    {
        return privateMinimum;
    }

    public void setMinimum(int value)
    {
        privateMinimum = value;
    }

    public IntegerValidation()
    {
        setMaximum(Integer.MAX_VALUE);
        setMinimum(Integer.MIN_VALUE);
    }

    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        String msg = "This field must contain integer number";
        if (value != null && value instanceof String && !((String) value).equals(""))
        {
            int intValue = 0;
            RefObject<Integer> tempRef_intValue = new RefObject<Integer>(intValue);
            boolean tempVar = !IntegerCompat.TryParse((String) value, tempRef_intValue);
            intValue = tempRef_intValue.argvalue;
            if (tempVar)
            {
                result.setSuccess(false);
                msg += StringFormat.format(" between %1$s and %2$s.", getMinimum(), getMaximum());
                result.getReasons().add(msg);
            }
            else if (intValue < getMinimum() || intValue > getMaximum())
            {
                if (getMinimum() != Integer.MIN_VALUE && getMaximum() != Integer.MAX_VALUE)
                {
                    msg += StringFormat.format(" between %1$s and %2$s", getMinimum(), getMaximum());
                }
                else if (getMinimum() != Integer.MIN_VALUE)
                {
                    msg += " greater than " + getMinimum();
                }
                else if (getMaximum() != Integer.MAX_VALUE)
                {
                    msg += " less than " + getMaximum();
                }

                result.setSuccess(false);
                result.getReasons().add(msg + ".");
            }
        }

        return result;
    }
}
