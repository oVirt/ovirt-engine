package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class CommandsFactoryTest {

    @Test
    public void testCommandPackagesAreSortedAlphabetically() {
        String[] commandPackages = CommandsFactory.getCommandPackages();
        String[] sortedCommandPackages = commandPackages.clone();
        Arrays.sort(sortedCommandPackages);
        assertArrayEquals(sortedCommandPackages, commandPackages, "The command packages are not sorted.");
    }
}
