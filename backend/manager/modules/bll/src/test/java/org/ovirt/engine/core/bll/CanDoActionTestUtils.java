package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Utilities for testing the {@link CommandBase#canDoAction()} command's behavior.
 */
public class CanDoActionTestUtils {

    /**
     * Run the canDoAction and assert that it fails with the given message, while printing the messages (for easier
     * debug if test fails).
     *
     * @param command
     *            The command to check canDoAction for.
     * @param message
     *            The message that should be in the failed messages.
     *
     * @return The failure messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertCanDoActionFailure(CommandBase<?> command, VdcBllMessages message) {
        return runAndAssertCanDoActionFailure("", command, message);
    }

    /**
     * Run the canDoAction and assert that it fails with the given message, while printing the messages (for easier
     * debug if test fails).
     *
     * @param assertionMessage
     *            The message to add to the assertion statement.
     * @param command
     *            The command to check canDoAction for.
     * @param message
     *            The message that should be in the failed messages.
     *
     * @return The failure messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertCanDoActionFailure
            (String assertionMessage, CommandBase<?> command, VdcBllMessages message) {
        assertFalse("Command's canDoAction expected to fail, but succeeded", command.canDoAction());

        return assertCanDoActionMessages(assertionMessage, command, message);
    }

    /**
     * Run the canDoAction and assert that it succeeds, while printing the messages (for easier debug if test fails).
     *
     * @param command
     *            The command to check canDoAction for.
     */
    public static void runAndAssertCanDoActionSuccess(CommandBase<?> command) {
        boolean canDoAction = command.canDoAction();
        List<String> canDoActionMessages = command.getReturnValue().getCanDoActionMessages();
        assertTrue(MessageFormat.format("Command''s canDoAction expected to succeed, but failed, messages are: {0}",
                canDoActionMessages), canDoAction);
        assertTrue(MessageFormat.format("Command''s canDoAction succeeded, but added the following messages: {0}",
                canDoActionMessages), canDoActionMessages.isEmpty());
    }

    /**
     * Run the canDoAction and assert that it contains the given messages, while printing the messages (for easier debug
     * if test fails).
     *
     * @param command
     *            The command to check canDoAction for.
     * @param messages
     *            The messages that should be set.
     *
     * @return The action messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertSetActionMessageParameters
            (CommandBase<?> command, VdcBllMessages... messages) {
        return runAndAssertSetActionMessageParameters("", command, messages);
    }

    /**
     * Run the canDoAction and assert that it contains the given messages, while printing the messages (for easier debug
     * if test fails).
     *
     * @param assertionMessage
     *            The message to add to the assertion statement.
     * @param command
     *            The command to check canDoAction for.
     * @param messages
     *            The messages that should be set.
     *
     * @return The action messages, so that they can be further examined if needed.
     */
    public static List<String> runAndAssertSetActionMessageParameters
            (String assertionMessage, CommandBase<?> command, VdcBllMessages... messages) {
        command.setActionMessageParameters();
        for (VdcBllMessages message : messages) {
            assertCanDoActionMessages(assertionMessage, command, message);
        }
        return command.getReturnValue().getCanDoActionMessages();
    }

    public static List<String> assertCanDoActionMessages(String assertionMessage,
            CommandBase<?> command,
            VdcBllMessages message) {
        List<String> canDoActionMessages = command.getReturnValue().getCanDoActionMessages();
        assertTrue(MessageFormat.format("{0}canDoAction messages doesn''t contain expected message: {1}, messages are: {2}",
                optionalMessage(assertionMessage),
                message.name(),
                canDoActionMessages),
                canDoActionMessages.contains(message.name()));

        return canDoActionMessages;
    }

    private static String optionalMessage(String assertionMessage) {
        if (!StringUtils.isEmpty(assertionMessage)) {
            assertionMessage += ". ";
        }
        return assertionMessage;
    }
}
