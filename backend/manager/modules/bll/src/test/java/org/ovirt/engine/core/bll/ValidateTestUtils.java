package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * Utilities for testing the {@link CommandBase#validate()} command's behavior.
 */
public class ValidateTestUtils {

    /**
     * Run the validate and assert that it fails with the given message, while printing the messages (for easier
     * debug if test fails).
     *
     * @param command
     *            The command to check validate for.
     * @param message
     *            The message that should be in the failed messages.
     *
     * @return The failure messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertValidateFailure(CommandBase<?> command, EngineMessage message) {
        return runAndAssertValidateFailure("", command, message);
    }

    /**
     * Run the validate and assert that it fails with the given message, while printing the messages (for easier
     * debug if test fails).
     *
     * @param assertionMessage
     *            The message to add to the assertion statement.
     * @param command
     *            The command to check validate for.
     * @param message
     *            The message that should be in the failed messages.
     *
     * @return The failure messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertValidateFailure
            (String assertionMessage, CommandBase<?> command, EngineMessage message) {
        assertFalse(command.validate(), "Command's validate expected to fail, but succeeded");

        return assertValidationMessages(assertionMessage, command, message);
    }

    /**
     * Run the validate and assert that it succeeds, while printing the messages (for easier debug if test fails).
     *
     * @param command
     *            The command to check validate for.
     */
    public static void runAndAssertValidateSuccess(CommandBase<?> command) {
        boolean validate = command.validate();
        List<String> validationMessages = command.getReturnValue().getValidationMessages();
        assertTrue(validate,
                MessageFormat.format("Command''s validate expected to succeed, but failed, messages are: {0}",
                        validationMessages));
        assertTrue(validationMessages.isEmpty(),
                MessageFormat.format("Command''s validate succeeded, but added the following messages: {0}",
                        validationMessages));
    }

    /**
     * Run the validate and assert that it contains the given messages, while printing the messages (for easier debug
     * if test fails).
     *
     * @param command
     *            The command to check validate for.
     * @param messages
     *            The messages that should be set.
     *
     * @return The action messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertSetActionMessageParameters
            (CommandBase<?> command, EngineMessage... messages) {
        return runAndAssertSetActionMessageParameters("", command, messages);
    }

    /**
     * Run the validate and assert that it contains the given messages, while printing the messages (for easier debug
     * if test fails).
     *
     * @param assertionMessage
     *            The message to add to the assertion statement.
     * @param command
     *            The command to check validate for.
     * @param messages
     *            The messages that should be set.
     *
     * @return The action messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertSetActionMessageParameters
            (String assertionMessage, CommandBase<?> command, EngineMessage... messages) {
        command.setActionMessageParameters();
        for (EngineMessage message : messages) {
            assertValidationMessages(assertionMessage, command, message);
        }
        return command.getReturnValue().getValidationMessages();
    }

    public static List<String> assertValidationMessages(String assertionMessage,
            CommandBase<?> command,
            EngineMessage message) {
        List<String> validationMessages = command.getReturnValue().getValidationMessages();
        assertTrue(validationMessages.contains(message.name()),
                MessageFormat.format("{0}validate messages doesn''t contain expected message: {1}, messages are: {2}",
                        optionalMessage(assertionMessage),
                        message.name(),
                        validationMessages));

        return validationMessages;
    }

    private static String optionalMessage(String assertionMessage) {
        if (!StringUtils.isEmpty(assertionMessage)) {
            assertionMessage += ". ";
        }
        return assertionMessage;
    }
}
