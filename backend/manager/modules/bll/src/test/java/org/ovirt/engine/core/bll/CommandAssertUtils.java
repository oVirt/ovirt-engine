package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A utility class for common command assertions */
public class CommandAssertUtils {
    public static void checkSucceeded(QueriesCommandBase<?> query, boolean expected) {
        assertEquals(expected, query.getQueryReturnValue().getSucceeded());
    }

    public static void checkSucceeded(CommandBase<?> cmd, boolean expected) {
        assertEquals(expected, cmd.getReturnValue().getSucceeded());
    }
}
