package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Used to represent the validation result for a "Can Do Action" validation operation, which can either return that the
 * validation succeeded, or that it failed with a specific message.<br>
 * This result can then be parsed by "Can Do Action" to decide if it should proceed or not.
 */
public final class ValidationResult {

    /**
     * A single instance for cases in which the outcome of the validation is ok.
     */
    public final static ValidationResult VALID = new ValidationResult();

    /**
     * In case the validation succeeded it is <code>null</code>, otherwise it contains the validation failure message.
     */
    private final VdcBllMessages message;

    /**
     * If there are any replacements for variables in the message, they can be set here.
     */
    private List<String> variableReplacements;

    /**
     * Default validation result is success with no message.
     * This constructor is private, it is only used to create a 'valid' result. Please use {@link ValidationResult#VALID}
     */
    private ValidationResult() {
        message = null;
    }

    /**
     * Validation result for failure with a given message.
     *
     * @param message
     *            The validation failure message.
     */
    public ValidationResult(VdcBllMessages message) {
        if(message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        this.message = message;
    }

    /**
     * Validation result for failure with a given message.
     *
     * @param message
     *            The validation failure message.
     * @param variableReplacements
     *            Replacements for variables that appear in the message, in syntax: "$var text" where $var is the
     *            variable to be replaced, and the text is the replacement.
     */
    public ValidationResult(VdcBllMessages message, String... variableReplacements) {
        this(message);
        this.variableReplacements = Collections.unmodifiableList(Arrays.asList(variableReplacements));
    }

    /**
     * Validation result for failure with a given message.
     *
     * @param message
     *            The validation failure message.
     * @param variableReplacements
     *            Replacements for variables that appear in the message, in syntax: "$var text" where $var is the
     *            variable to be replaced, and the text is the replacement.
     */
    public ValidationResult(VdcBllMessages message, Collection<String> variableReplacements) {
        this(message, variableReplacements.toArray(new String[variableReplacements.size()]));
    }

    /**
     * @return Did the validation succeed or not?
     */
    public boolean isValid() {
        return message == null;
    }

    /**
     * @return <code>null</code> in case the validation succeeded, otherwise the validation failure message
     */
    public VdcBllMessages getMessage() {
        return message;
    }

    /**
     * @return <code>null</code> in case there are no replacements, otherwise a list of the replacements for message
     *         variables.
     */
    public List<String> getVariableReplacements() {
        return variableReplacements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
        result = prime * result + ((getVariableReplacements() == null) ? 0 : getVariableReplacements().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValidationResult other = (ValidationResult) obj;
        if (getMessage() != other.getMessage())
            return false;
        if (getVariableReplacements() == null) {
            if (other.getVariableReplacements() != null)
                return false;
        } else if (!getVariableReplacements().equals(other.getVariableReplacements()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValidationResult [message=")
                .append(getMessage())
                .append(", variableReplacements=")
                .append(getVariableReplacements())
                .append("]");
        return builder.toString();
    }

}
