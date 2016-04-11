package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * Used to represent the validation result for a "Can Do Action" validation operation, which can either return that the
 * validation succeeded, or that it failed with a specific message.<br>
 * This result can then be parsed by "Can Do Action" to decide if it should proceed or not.
 */
public final class ValidationResult {

    /**
     * A single instance for cases in which the outcome of the validation is ok.
     */
    public static final ValidationResult VALID = new ValidationResult();

    /**
     * In case the validation succeeded it is {@code null}, otherwise it contains the validation failure message.
     */
    private final EngineMessage message;

    /**
     * If there are any replacements for variables in the message, they can be set here.
     */
    private final List<String> variableReplacements;

    /**
     * Default validation result is success with no message.
     * This constructor is private, it is only used to create a 'valid' result. Please use {@link ValidationResult#VALID}
     */
    private ValidationResult() {
        message = null;
        variableReplacements = Collections.emptyList();
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
    public ValidationResult(EngineMessage message, String... variableReplacements) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        this.message = message;
        this.variableReplacements = variableReplacements == null || variableReplacements.length == 0 ?
                Collections.<String>emptyList() :
                Collections.unmodifiableList(Arrays.asList(variableReplacements));
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
    public ValidationResult(EngineMessage message, Collection<String> variableReplacements) {
        this(message, variableReplacements.toArray(new String[variableReplacements.size()]));
    }

    /**
     * @return Did the validation succeed or not?
     */
    public boolean isValid() {
        return message == null;
    }

    /**
     * @return {@code null} in case the validation succeeded, otherwise the validation failure message
     */
    public EngineMessage getMessage() {
        return message;
    }

    /**
     * @return {@code null} in case there are no replacements, otherwise a list of the replacements for message
     *         variables.
     */
    public List<String> getVariableReplacements() {
        return variableReplacements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                message,
                variableReplacements
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValidationResult)) {
            return false;
        }
        ValidationResult other = (ValidationResult) obj;
        return Objects.equals(message, other.message)
                && Objects.equals(variableReplacements, other.variableReplacements);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("message", getMessage())
                .append("variableReplacements", getVariableReplacements())
                .build();
    }

    /**
     * Return an error if the following validation is not successful, or a valid result if it is.<br>
     * <br>
     * For example, if we want to make sure that <b>num doesn't equal 2</b> then we would do:
     *
     * <pre>
     * int num = 1;
     * ValidationResult.failWith(EngineMessage.ERROR_CONST).when(num == 2);
     * </pre>
     *
     * <br>
     * Which would return a valid result since 1 != 2.<br>
     * If we were to set <b>num = 2</b> in the example then the desired validation error would return.<br>
     * <br>
     * Conveniently, we can also check that <b>num equals 2</b> by doing:
     *
     * <pre>
     * int num = 1;
     * ValidationResult.failWith((EngineMessage.ERROR_CONST, &quot;$COMPARED_NUM 2&quot;).unless(num == 2);
     * </pre>
     *
     * <br>
     * This time the desired validation error would return since 1 != 2.<br>
     * If we were to set <b>num = 2</b> in the example then the result would be valid.<br>
     * In addition, the replacement will contain the substitutions for the message.<br>
     *
     * @param expectedError
     *            The error we expect should the validation fail.
     * @param replacements
     *            The replacements to be associated with the validation result
     * @return A helper object that returns the correct validation result depending on the condition.
     */
    public static ValidationResultBuilder failWith(EngineMessage expectedError, String... replacements) {
        return new ValidationResultBuilder(expectedError, replacements);
    }

    public static ValidationResultBuilder failWith(EngineMessage expectedError, Collection<String> replacements) {
        return new ValidationResultBuilder(expectedError, replacements.toArray(new String[replacements.size()]));
    }

    /**
     * Helper class to chain calls that produce a {@link ValidationResult}.
     */
    public static class ValidationResultBuilder {

        private ValidationResult expectedValidation;

        private ValidationResultBuilder(EngineMessage expectedError, String... replacements) {
            expectedValidation = new ValidationResult(expectedError, replacements);
        }

        /**
         * Return the expected error when the condition occurs, or a valid result if it doesn't.<br>
         * <br>
         * For example, if we want to make sure that <b>num doesn't equal 2</b> then we would do:
         * <pre>
         *     int num = 1;
         *     ValidationResult.failWith((EngineMessage.ERROR_CONST).when(num == 2);</pre>
         * <br>
         * Which would return a valid result since 1 != 2.<br>
         * If we were to set <b>num = 2</b> in the example then the desired validation error would return.<br>
         *
         * @param conditionOccurs
         *            Indication if the condition for the validation occurs or not.
         * @return The erroneous validation result if the condition occurs, or a valid result of it doesn't.
         */
        public ValidationResult when(boolean conditionOccurs) {
            return conditionOccurs ? expectedValidation : VALID;
        }

        /**
         * Return the expected error unless the condition occurs, or a valid result if it does.<br>
         * <br>
         * For example, if we want to make sure that <b>num equals 2</b> then we would do:
         * <pre>
         *     int num = 1;
         *     ValidationResult.failWith(EngineMessage.ERROR_CONST).unless(num == 2);</pre>
         * <br>
         * Which would return the desired validation error since 1 != 2.<br>
         * If we were to set <b>num = 2</b> in the example then the result would be valid.<br>
         *
         * @param conditionOccurs
         *            Indication if the condition for the validation occurs or not.
         * @return A valid result if the condition occurs, or the erroneous validation result if it doesn't.
         */
        public ValidationResult unless(boolean conditionOccurs) {
            return when(!conditionOccurs);
        }
    }
}
