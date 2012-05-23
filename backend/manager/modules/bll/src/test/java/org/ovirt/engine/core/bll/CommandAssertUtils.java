package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.ovirt.engine.core.dal.VdcBllMessages;

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
        assertEquals(expected.length, returned.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), returned.get(i));
        }
    }

}
