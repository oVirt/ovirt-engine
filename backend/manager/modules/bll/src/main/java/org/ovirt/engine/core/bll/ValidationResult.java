package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final List<EngineMessage> messages = new ArrayList<>();

    /**
     * If there are any replacements for variables in the message, they can be set here.
     */
    private final List<String> variableReplacements;

    /**
     * Default validation result is success with no message.
     * This constructor is private, it is only used to create a 'valid' result. Please use {@link ValidationResult#VALID}
     */
    private ValidationResult() {
        variableReplacements = Collections.emptyList();
    }

    /**
     * Validation result for failure with given messages.
     *
     * @param messages
     *            The validation failure messages.
     * @param variableReplacements
     *            Replacements for variables that appear in the messages, in syntax: "$var text" where $var is the
     *            variable to be replaced, and the text is the replacement.
     */
    public ValidationResult(List<EngineMessage> messages, String... variableReplacements) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }

        this.messages.addAll(messages);
        this.variableReplacements = variableReplacements == null || variableReplacements.length == 0 ?
                Collections.emptyList() :
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
    public ValidationResult(EngineMessage message, String... variableReplacements) {
        this(Collections.singletonList(message), variableReplacements);
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
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
        return messages.isEmpty();
    }

    /**
     * @return an empty {@code List} in case the validation succeeded, otherwise the validation failure messages
     */
    public List<EngineMessage> getMessages() {
        return messages;
    }

    /**
     * @return an empty {@code List} in case the validation succeeded, otherwise the validation
     * failure messages as Strings
     */
    public List<String> getMessagesAsStrings() {
        return messages.stream().map(Enum::name).collect(Collectors.toList());
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
                messages,
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
        return Objects.equals(messages, other.messages)
                && Objects.equals(variableReplacements, other.variableReplacements);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("messages", messages.toArray())
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

        private EngineMessage expectedError;
        private String[] replacements;

        private ValidationResultBuilder(EngineMessage expectedError, String... replacements) {
            this.expectedError = expectedError;
            this.replacements = replacements;
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
            return conditionOccurs ? new ValidationResult(expectedError, replacements) : VALID;
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
