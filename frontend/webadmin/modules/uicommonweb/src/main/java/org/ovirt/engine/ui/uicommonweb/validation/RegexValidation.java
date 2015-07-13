package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.Regex;

@SuppressWarnings("unused")
public class RegexValidation implements IValidation {
    private String privateExpression;

    public String getExpression() {
        return privateExpression;
    }

    public void setExpression(String value) {
        privateExpression = value;
    }

    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private boolean privateIsNegate;

    public boolean getIsNegate() {
        return privateIsNegate;
    }

    public void setIsNegate(boolean value) {
        privateIsNegate = value;
    }

    public RegexValidation() {
    }

    public RegexValidation(String expression, String message) {
        setExpression(expression);
        setMessage(message);
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value == null) {
            value = ""; //$NON-NLS-1$
        }

        if (value != null
                && value instanceof String
                && (getIsNegate() ? Regex.IsMatch(value.toString(), getExpression())
                        : !Regex.IsMatch(value.toString(), getExpression()))) {
            result.setSuccess(false);
            result.getReasons().add(getMessage());
        }

        return result;
    }
}
