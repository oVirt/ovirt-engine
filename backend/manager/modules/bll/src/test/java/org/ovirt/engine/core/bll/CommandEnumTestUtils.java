package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public class CommandEnumTestUtils {
    public static <E extends Enum<E>, C> void testCommandsExist(Class<E> enumClass, Function<E, C> commandCreator) {

        List<E> missingCommands =
                EnumSet.allOf(enumClass)
                        .stream()
                        .filter(e -> !e.name().equals("Unknown"))
                        .filter(e -> commandCreator.apply(e) == null)
                        .collect(Collectors.toList());

        assertTrue(missingCommands.isEmpty(),
                enumClass.getSimpleName() +
                        " contains the following values that does not correspond to an existing commands: " +
                        missingCommands);
    }

    public static <E extends Enum<E>, C> void testCommandClassHasEnum
            (Class<E> enumClass, Class<C> commandClass, String suffix) {

        final Reflections reflections = new Reflections(CommandsFactory.getCommandPackages());
        Set<Class<? extends C>> commands = reflections.getSubTypesOf(commandClass);

        Set<String> commandsWithoutEnum =
                commands.stream()
                        .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                        .filter(c -> c.getEnclosingClass() == null)
                        .filter(c -> {
                            try {
                                // Fails with an IllegalArgumentException if there's no such member
                                Enum.valueOf(enumClass, c.getSimpleName().replaceAll(suffix + "$", ""));
                                return false;
                            } catch (IllegalArgumentException ignore) {
                                return true;
                            }
                        }).map(Class::getName)
                        .collect(Collectors.toSet());

        assertTrue(commandsWithoutEnum.isEmpty(),
                "Found the following commands without a corresponding " + enumClass.getSimpleName() +
                        " constant : " + commandsWithoutEnum);

    }
}
