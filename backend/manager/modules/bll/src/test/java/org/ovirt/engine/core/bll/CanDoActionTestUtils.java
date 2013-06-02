package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.dal.VdcBllMessages;

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
        boolean canDoAction = command.canDoAction();
        List<String> canDoActionMessages = command.getReturnValue().getCanDoActionMessages();

        assertFalse("Command's canDoAction expected to fail, but succeeded", canDoAction);
        assertTrue(MessageFormat.format("canDoAction messages doesn''t contain expected message: {0}, messages are: {1}",
                message.name(),
                canDoActionMessages),
                canDoActionMessages.contains(message.name()));

        return canDoActionMessages;
    }

    /**
     * Run the canDoAction and assert that it succeeds, while printing the messages (for easier debug if test fails).
     *
     * @param command
     *            The command to check canDoAction for.
     */
    public static void runAndAssertCanDoActionSuccess(CommandBase<?> command) {
        boolean canDoAction = command.canDoAction();
        assertTrue(MessageFormat.format("Command''s canDoAction expected to succeed, but failed, messages are: {0}",
                command.getReturnValue().getCanDoActionMessages()), canDoAction);
    }
}
