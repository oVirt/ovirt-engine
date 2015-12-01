package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class CommandsFactoryTest {

    private CommandsFactory commandFactory;

    @Before
    public void setUp() {
        commandFactory = new CommandsFactory();
    }

    @Test
    public void testCommandPackagesAreSortedAlphabetically() {
        String[] commandPackages = commandFactory.getCommandPackages();
        String[] sortedCommandPackages = commandPackages.clone();
        Arrays.sort(sortedCommandPackages);
        assertArrayEquals("The command packages are not sorted.", sortedCommandPackages, commandPackages);
    }
}
