package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.aaa.CreateUserSessionCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;

public class CommandsFactoryTest {

    @Test
    public void testCommandPackagesAreSortedAlphabetically() {
        String[] commandPackages = CommandsFactory.getCommandPackages();
        String[] sortedCommandPackages = commandPackages.clone();
        Arrays.sort(sortedCommandPackages);
        assertArrayEquals(sortedCommandPackages, commandPackages, "The command packages are not sorted.");
    }

    @Test
    public void testConstructorCacheDoesNotGenerateMemoryLeak() {
        CommandsFactory.getCommandConstructor(CreateUserSessionCommand.class,
                CreateUserSessionParameters.class,
                CommandContext.class);
        CommandsFactory.getCommandConstructor(CreateUserSessionCommand.class,
                CreateUserSessionParameters.class,
                CommandContext.class);
        CommandsFactory.getCommandConstructor(CreateUserSessionCommand.class,
                CreateUserSessionParameters.class,
                CommandContext.class);

        assertEquals(1, CommandsFactory.getConstructorCacheSize());
    }
}
