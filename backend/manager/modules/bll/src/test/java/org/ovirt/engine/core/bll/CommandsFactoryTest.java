package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

public class CommandsFactoryTest {

    @Test
    public void testCommandPackagesAreSortedAlphabetically() {
        String[] commandPackages = CommandsFactory.getCommandPackages();
        String[] sortedCommandPackages = commandPackages.clone();
        Arrays.sort(sortedCommandPackages);
        assertArrayEquals("The command packages are not sorted.", sortedCommandPackages, commandPackages);
    }
}
