package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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

    public IntegerValidation(int min, int max) {
        setMinimum(min);
        setMaximum(max);
    }

    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && !((String) value).equals("")) //$NON-NLS-1$
        {
            int intValue = 0;
            RefObject<Integer> tempRef_intValue = new RefObject<Integer>(intValue);
            boolean tempVar = !IntegerCompat.TryParse((String) value, tempRef_intValue);
            intValue = tempRef_intValue.argvalue;
            String msg = ""; //$NON-NLS-1$
            String prefixMsg =
                    ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason();
            if (tempVar)
            {
                result.setSuccess(false);
                msg =
                        ConstantsManager.getInstance()
                                .getMessages()
                                .integerValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                result.getReasons().add(msg);
            }
            else if (intValue < getMinimum() || intValue > getMaximum())
            {
                if (getMinimum() != Integer.MIN_VALUE && getMaximum() != Integer.MAX_VALUE)
                {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .integerValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                }
                else if (getMinimum() != Integer.MIN_VALUE)
                {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .integerValidationNumberGreaterInvalidReason(prefixMsg, getMinimum());
                }
                else if (getMaximum() != Integer.MAX_VALUE)
                {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .integerValidationNumberLessInvalidReason(prefixMsg, getMaximum());
                }

                result.setSuccess(false);
                result.getReasons().add(msg);
            }
        }

        return result;
    }
}
