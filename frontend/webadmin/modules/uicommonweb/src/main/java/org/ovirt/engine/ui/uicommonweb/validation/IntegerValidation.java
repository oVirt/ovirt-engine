package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class IntegerValidation implements IValidation {
    private int privateMaximum;

    public int getMaximum() {
        return privateMaximum;
    }

    public void setMaximum(int value) {
        privateMaximum = value;
    }

    private int privateMinimum;

    public int getMinimum() {
        return privateMinimum;
    }

    public void setMinimum(int value) {
        privateMinimum = value;
    }

    public IntegerValidation() {
        setMaximum(Integer.MAX_VALUE);
        setMinimum(Integer.MIN_VALUE);
    }

    public IntegerValidation(int min, int max) {
        setMinimum(min);
        setMaximum(max);
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value != null && ((value instanceof String && !value.equals("")) || value instanceof Integer)) { //$NON-NLS-1$
            Integer intValue = value instanceof String ? IntegerCompat.tryParse((String) value) : (Integer) value;
            String msg = ""; //$NON-NLS-1$
            String prefixMsg =
                    ConstantsManager.getInstance().getConstants().thisFieldMustContainIntegerNumberInvalidReason();
            if (intValue == null) {
                result.setSuccess(false);
                msg =
                        ConstantsManager.getInstance()
                                .getMessages()
                                .integerValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                result.getReasons().add(msg);
            } else if (intValue < getMinimum() || intValue > getMaximum()) {
                if (getMinimum() != Integer.MIN_VALUE && getMaximum() != Integer.MAX_VALUE) {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .integerValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                } else if (getMinimum() != Integer.MIN_VALUE) {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .integerValidationNumberGreaterInvalidReason(prefixMsg, getMinimum());
                } else if (getMaximum() != Integer.MAX_VALUE) {
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
