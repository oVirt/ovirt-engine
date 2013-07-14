package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.ovirt.engine.core.common.errors.VdcBllMessages;

/** A utility class for common command assertions */
public class CommandAssertUtils {
    public static void checkSucceeded(QueriesCommandBase<?> query, boolean expected) {
        assertEquals(expected, query.getQueryReturnValue().getSucceeded());
    }

    public static void checkSucceeded(CommandBase<?> cmd, boolean expected) {
        assertEquals(expected, cmd.getReturnValue().getSucceeded());
    }

    public static void checkMessages(CommandBase<?> cmd, VdcBllMessages... expected) {
        List<String> returned = cmd.getReturnValue().getCanDoActionMessages();
        assertEquals("Wrong number of messages", expected.length, returned.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Wrong message", expected[i].toString(), returned.get(i));
        }
    }

    /**
     * This method checks if the return CDA messages contain the expected messages, it is different from checkMessages
     * by the fact that it does not check by the order the parameters were given, the order is irrelevant most of the
     * time and it does not check that the size of the returned messages matches the size of the expected messages, this
     * comes in handy for example in scenarios where the CDA messages return a resolved parameters (e.g. '$VmName MyVM')
     */
    public static void checkMessagesContains(CommandBase<?> cmd, VdcBllMessages... expected) {
        List<String> cdaMessages = cmd.getReturnValue().getCanDoActionMessages();
        for (int i = 0; i < expected.length; i++) {
            assertTrue("CanDoAction message does not contain the message " + expected[i],
                    cdaMessages.contains(expected[i].toString()));
        }
    }
}
