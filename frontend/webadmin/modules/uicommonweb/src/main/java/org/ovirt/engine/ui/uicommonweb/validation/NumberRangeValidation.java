package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public abstract class NumberRangeValidation<T extends Number> implements IValidation {
    private T maximum;

    public T getMaximum() {
        return maximum;
    }

    public void setMaximum(T value) {
        maximum = value;
    }

    private T minimum;

    public T getMinimum() {
        return minimum;
    }

    public void setMinimum(T value) {
        minimum = value;
    }

    public NumberRangeValidation(T min, T max) {
        setMaximum(max);
        setMinimum(min);
    }

    /***
     * Parse a given string to type T
     * @param value
     *            a {@link String} representation of the required value
     * @return T initiated with value in case of success, null otherwise
     */
    protected abstract T parse(String value);

    protected abstract boolean belowMinimum(T value);

    protected abstract boolean aboveMaximum(T value);

    /***
     * @return A {@link String} of the required number type name represented in human-readable constant which will be
     *         presented via application's user interface.
     */
    protected abstract String getValueTypeName();

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();
        T parsedValue = null;
        if (value instanceof String && !StringUtils.isEmpty((String) value)) {
            parsedValue = parse((String) value);
        } else {
            try {
                parsedValue = (T) value;
            } catch (Exception ex) {
                validationFailedWith(result, getNumberTypeErrorMessage());
                return result;
            }
        }
        String message = ""; //$NON-NLS-1$
        String prefixMsg = getNumberTypeErrorMessage();
        if (parsedValue == null) {
            message = getNumberTypeErrorMessage();
            validationFailedWith(result, message);
        } else if (belowMinimum(parsedValue)) {
            message = getNumberBellowMinimumErrorMessage(prefixMsg);
            validationFailedWith(result, message);
        } else if (aboveMaximum(parsedValue)) {
            message = getNumberAboveMaximumErrorMessage(prefixMsg);
            validationFailedWith(result, message);
        }
        return result;
    }

    public String getNumberAboveMaximumErrorMessage(String prefixMsg) {
        return getMessages().numberValidationNumberLessInvalidReason(prefixMsg, getMaximum().toString());
    }

    protected String getNumberBellowMinimumErrorMessage(String prefixMsg) {
        return getMessages().numberValidationNumberGreaterInvalidReason(prefixMsg, getMinimum().toString());
    }

    protected String getNumberRangeErrorMessage(String prefixMsg) {
        return getMessages().numberValidationNumberBetweenInvalidReason(prefixMsg,
                getMinimum().toString(),
                getMaximum().toString());
    }

    protected String getNumberTypeErrorMessage() {
        return getMessages().thisFieldMustContainTypeNumberInvalidReason(getValueTypeName());
    }

    protected UIMessages getMessages() {
        return getConstManagerInstance().getMessages();
    }

    public ConstantsManager getConstManagerInstance() {
        return ConstantsManager.getInstance();
    }

    private void validationFailedWith(ValidationResult result, String msg) {
        result.setSuccess(false);
        result.getReasons().add(msg);
    }
}
