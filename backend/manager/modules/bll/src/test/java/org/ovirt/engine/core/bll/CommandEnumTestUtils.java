package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandEnumTestUtils {
    public static <E extends Enum<E>, C> void testCommandsExist(Class<E> enumClass, Function<E, C> commandCreator) {

        List<E> missingCommands =
                EnumSet.allOf(enumClass)
                        .stream()
                        .filter(e -> !e.name().equals("Unknown"))
                        .filter(e -> commandCreator.apply(e) == null)
                        .collect(Collectors.toList());

        assertTrue(enumClass.getSimpleName() +
                        " contains the following values that does not correspond to an existing commands: " +
                        missingCommands,
                missingCommands.isEmpty());
    }
}
