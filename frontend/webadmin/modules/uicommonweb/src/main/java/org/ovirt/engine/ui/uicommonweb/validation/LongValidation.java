package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LongValidation implements IValidation {

    private long maximum;
    private long minimum;

    public long getMaximum() {
        return maximum;
    }

    public void setMaximum(long maximum) {
        this.maximum = maximum;
    }

    public long getMinimum() {
        return minimum;
    }

    public void setMinimum(long minimum) {
        this.minimum = minimum;
    }

    public LongValidation() {
        setMaximum(Long.MAX_VALUE);
        setMinimum(Long.MIN_VALUE);
    }

    public LongValidation(long min, long max) {
        setMinimum(min);
        setMaximum(max);
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value != null && ((value instanceof String && !"".equals(value)) || value instanceof Long)) { //$NON-NLS-1$
            // Do not use org.apache.commons.lang.math.NumberUtils. Null is expected if conversion fails.
            Long longValue = value instanceof String ? tryParseLong((String) value) : (Long) value;
            String msg = ""; //$NON-NLS-1$
            String prefixMsg =
                    ConstantsManager.getInstance().getConstants().thisFieldMustContainNumberInvalidReason();
            if (longValue == null) {
                result.setSuccess(false);
                msg =
                        ConstantsManager.getInstance()
                                .getMessages()
                                .longValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                result.getReasons().add(msg);
            } else if (longValue < getMinimum() || longValue > getMaximum()) {
                if (getMinimum() != Long.MIN_VALUE && getMaximum() != Long.MAX_VALUE) {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .longValidationNumberBetweenInvalidReason(prefixMsg, getMinimum(), getMaximum());
                } else if (getMinimum() != Long.MIN_VALUE) {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .longValidationNumberGreaterInvalidReason(prefixMsg, getMinimum());
                } else if (getMaximum() != Long.MAX_VALUE) {
                    msg =
                            ConstantsManager.getInstance()
                                    .getMessages()
                                    .longValidationNumberLessInvalidReason(prefixMsg, getMaximum());
                }

                result.setSuccess(false);
                result.getReasons().add(msg);
            }
        }

        return result;
    }

    private Long tryParseLong(final String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
